package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.ErrorDetail;
import io.electrum.giftcard.api.model.VoidRequest;
import io.electrum.giftcard.api.model.VoidResponse;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.ActivationRecord;
import io.electrum.giftcard.server.backend.records.ActivationReversalRecord;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.CardRecord.Status;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.tables.VoidsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;

public class VoidHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, VoidRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      VoidRecord voidRecord = null;
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateVoidRequest(request);
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
         VoidsTable voidsTable = giftcardDb.getVoidsTable();
         voidRecord = new VoidRecord(requestId.toString());
         voidRecord.setVoidRequest(request);
         voidsTable.putRecord(voidRecord);
         // check card can be activated
         rsp = canVoidCard(giftcardDb, request);
         if (rsp != null) {
            return rsp;
         }
         // do the void
         ErrorDetail voidError = voidCard(request, giftcardDb);
         if (voidError != null) {
            rsp = Response.status(400).entity(voidError).build();
            return rsp;
         }
         // record response
         VoidResponse voidRsp = GiftcardModelUtils.voidRspFromReq(giftcardDb, request);
         voidRecord.setVoidResponse(voidRsp);
         // respond
         return Response.status(201).entity(voidRsp).build();
      } catch (Exception e) {
         log.debug("error processing VoidRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(GiftcardModelUtils.exceptionResponse()).build();
         return rsp;
      } finally {
         if (voidRecord != null) {
            voidRecord.setResponded();
         }
      }
   }

   private Response canVoidCard(MockGiftcardDb giftcardDb, VoidRequest request) {
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
            activationReversalRecord = giftcardDb.getActivationReversalsTable().getRecord(activationRecord.getLastReversalId());
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

   private ErrorDetail voidCard(VoidRequest request, MockGiftcardDb giftcardDb) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      cardRecord.setStatus(Status.VOIDED);
      cardRecord.setVoidId(request.getId().toString());
      return null;
   }
}
