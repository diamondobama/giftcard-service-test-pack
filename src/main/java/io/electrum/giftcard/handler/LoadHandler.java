package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.ErrorDetail;
import io.electrum.giftcard.api.model.LoadRequest;
import io.electrum.giftcard.api.model.LoadResponse;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.ActivationRecord;
import io.electrum.giftcard.server.backend.records.ActivationReversalRecord;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.LoadRecord;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.tables.LoadsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.LedgerAmount;

public class LoadHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, LoadRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      LoadRecord loadRecord = null;
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateLoadRequest(request);
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
         LoadsTable loadsTable = giftcardDb.getLoadsTable();
         loadRecord = new LoadRecord(requestId.toString());
         loadRecord.setLoadRequest(request);
         loadsTable.putRecord(loadRecord);
         // check card can be activated
         rsp = canLoadCard(giftcardDb, request);
         if (rsp != null) {
            return rsp;
         }
         // do the load
         ErrorDetail loadError = load(request, giftcardDb);
         if (loadError != null) {
            rsp = Response.status(400).entity(loadError).build();
            return rsp;
         }
         // record response
         LoadResponse loadRsp = GiftcardModelUtils.loadRspFromReq(giftcardDb, request);
         loadRecord.setLoadResponse(loadRsp);
         // respond
         return Response.status(201).entity(loadRsp).build();
      } catch (Exception e) {
         log.debug("error processing LoadRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(GiftcardModelUtils.exceptionResponse()).build();
         return rsp;
      } finally {
         if (loadRecord != null) {
            loadRecord.setResponded();
         }
      }
   }

   private Response canLoadCard(MockGiftcardDb giftcardDb, LoadRequest request) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      if (cardRecord == null) {
         return Response.status(400).entity(GiftcardModelUtils.cardNotFound(request)).build();
      }
      switch (cardRecord.getStatus()) {
      case NEW:
      case ACTIVATED:
         ActivationRecord activationRecord = giftcardDb.getActivationsTable().getRecord(cardRecord.getActivationId());
         ActivationReversalRecord activationReversalRecord = null;
         if (activationRecord != null) {
            activationReversalRecord =
                  giftcardDb.getActivationReversalsTable().getRecord(activationRecord.getLastReversalId());
         }
         return Response.status(400)
               .entity(GiftcardModelUtils.cardIsNotYetActive(cardRecord, activationRecord, activationReversalRecord))
               .build();
      case ACTIVATED_CONFIRMED:
         break;
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

   private ErrorDetail load(LoadRequest request, MockGiftcardDb giftcardDb) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      cardRecord.addLoadId(request.getId().toString());
      LedgerAmount bookBalance = cardRecord.getBalance();
      long currentBalance = bookBalance.getAmount();
      long requestAmount = request.getAmounts().getRequestAmount().getAmount();
      bookBalance.setAmount(currentBalance + requestAmount);
      return null;
   }
}
