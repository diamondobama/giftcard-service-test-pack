package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.ActivationRequest;
import io.electrum.giftcard.api.model.ActivationResponse;
import io.electrum.giftcard.api.model.ErrorDetail;
import io.electrum.giftcard.resource.impl.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.ActivationRecord;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.CardRecord.Status;
import io.electrum.giftcard.server.backend.records.ProductRecord;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.tables.ActivationsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.Amounts;
import io.electrum.vas.model.LedgerAmount;

public class ActivationHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, ActivationRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      ActivationRecord activationRecord = null;
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateActivationRequest(request);
         if (rsp != null) {
            return rsp;
         }
         rsp = GiftcardModelUtils.isUuidConsistent(requestId, request);
         if (rsp != null) {
            return rsp;
         }
         // get the DB for this user
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         // check for duplicates
         if (giftcardDb.doesUuidExist(requestId.toString())) {
            return Response.status(400).entity(GiftcardModelUtils.duplicateRequest(requestId.toString())).build();
         }
         // record request
         ActivationsTable activationsTable = giftcardDb.getActivationsTable();
         activationRecord = new ActivationRecord(requestId.toString());
         activationRecord.setActivationRequest(request);
         activationsTable.putRecord(activationRecord);
         // check card can be activated
         rsp = canActivateCard(giftcardDb, request);
         if (rsp != null) {
            return rsp;
         }
         // do the activation
         ErrorDetail activateError = activate(request, giftcardDb);
         if (activateError != null) {
            rsp = Response.status(400).entity(activateError).build();
            return rsp;
         }
         // record response
         ActivationResponse activationRsp = GiftcardModelUtils.activationRspFromReq(giftcardDb, request);
         activationRecord.setActivationResponse(activationRsp);
         // respond
         return Response.status(201).entity(activationRsp).build();
      } catch (Exception e) {
         log.debug("error processing ActivationRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(GiftcardModelUtils.exceptionResponse()).build();
         return rsp;
      } finally {
         if (activationRecord != null) {
            activationRecord.setResponded();
         }
      }
   }

   private Response canActivateCard(MockGiftcardDb giftcardDb, ActivationRequest request) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      if (cardRecord == null) {
         return Response.status(400).entity(GiftcardModelUtils.cardNotFound(request)).build();
      }
      ProductRecord productRecord = giftcardDb.getProductRecord(request.getProduct());
      if (productRecord == null) {
         return Response.status(400).entity(GiftcardModelUtils.productNotFound(request)).build();
      }
      switch (cardRecord.getStatus()) {
      case NEW:
         break;
      case ACTIVATED:
      case ACTIVATED_CONFIRMED:
         ActivationRecord activationRecord = giftcardDb.getActivationsTable().getRecord(cardRecord.getActivationId());
         return Response.status(400).entity(GiftcardModelUtils.cardIsActive(cardRecord, activationRecord)).build();
      case VOIDED:
      case VOIDED_CONFIRMED:
         VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(cardRecord.getVoidId());
         return Response.status(400).entity(GiftcardModelUtils.cardIsVoided(cardRecord, voidRecord)).build();
      }
      if (!cardRecord.expiryDateCorrect(request.getCard().getExpiryDate())) {
         return Response.status(400).entity(GiftcardModelUtils.cardExpiryInvalid(cardRecord, request)).build();
      }
      if (cardRecord.cardExpired()) {
         return Response.status(400).entity(GiftcardModelUtils.cardExpired(request)).build();
      }
      return null;
   }

   private ErrorDetail activate(ActivationRequest request, MockGiftcardDb giftcardDb) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      ProductRecord productRecord = giftcardDb.getProductRecord(request.getProduct());
      Amounts amounts = request.getAmounts();
      long requestAmount = 0;
      if (amounts != null) {
         requestAmount = amounts.getRequestAmount().getAmount();
      }
      cardRecord.setBalance(
            new LedgerAmount().amount(requestAmount + productRecord.getStartingBalance().getAmount())
                  .currency(productRecord.getStartingBalance().getCurrency()));
      cardRecord.setStatus(Status.ACTIVATED);
      cardRecord.setActivationId(request.getId().toString());
      cardRecord.setProductId(productRecord.getRecordId());
      //update with new PIN if submitted
      //clear PIN takes preference
      String clearPin = request.getCard().getClearPin();
      if(clearPin != null && !clearPin.isEmpty())
      {
         cardRecord.getCard().setClearPin(clearPin);
      }
      else
      {
         String encPin = request.getCard().getEncryptedPin();
         if(encPin != null && !encPin.isEmpty())
         {
            cardRecord.getCard().setEncryptedPin(encPin);
         }
      }
      return null;
   }
}
