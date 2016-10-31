package io.electrum.giftcard.handler;

import io.electrum.giftcard.api.model.VoidConfirmation;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.CardRecord.Status;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.records.VoidConfirmationRecord;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.records.VoidReversalRecord;
import io.electrum.giftcard.server.backend.tables.VoidConfirmationsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.BasicAdviceResponse;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmVoidHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(
         String requestId,
         String confirmationId,
         VoidConfirmation confirmation,
         HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateVoidConfirmation(confirmation);
         if (rsp != null) {
            return rsp;
         }
         rsp = GiftcardModelUtils.isUuidConsistent(requestId, confirmationId, confirmation);
         if (rsp != null) {
            return rsp;
         }
         // get the DB for this user
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         // record request
         if (giftcardDb.doesUuidExist(confirmationId)) {
            return Response.status(400)
                  .entity(GiftcardModelUtils.duplicateRequest(confirmation, confirmationId))
                  .build();
         }
         VoidConfirmationsTable voidConfirmationsTable = giftcardDb.getVoidConfirmationsTable();
         VoidConfirmationRecord voidConfirmationRecord = new VoidConfirmationRecord(confirmationId);
         voidConfirmationRecord.setRequestId(requestId);
         voidConfirmationRecord.setVoidConfirmation(confirmation);
         voidConfirmationsTable.putRecord(voidConfirmationRecord);
         VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(requestId);
         if (voidRecord != null) {
            voidRecord.addConfirmationId(confirmation.getId());
         }
         // process request
         rsp = canConfirmVoid(confirmation, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         BasicAdviceResponse adviceResponse = confirmVoid(confirmation, giftcardDb);
         // respond
         return Response.accepted().entity(adviceResponse).build();
      } catch (Exception e) {
         log.debug("error processing VoidConfirmation", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private BasicAdviceResponse confirmVoid(VoidConfirmation confirmation, MockGiftcardDb giftcardDb) {
      VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(confirmation.getRequestId());
      voidRecord.setState(State.CONFIRMED);
      CardRecord cardRecord = giftcardDb.getCardRecord(voidRecord.getVoidRequest().getCard());
      cardRecord.setStatus(Status.VOIDED_CONFIRMED);

      return new BasicAdviceResponse().id(confirmation.getId())
            .requestId(confirmation.getRequestId())
            .time(confirmation.getTime())
            .transactionIdentifiers(confirmation.getThirdPartyIdentifiers());
   }

   private Response canConfirmVoid(VoidConfirmation confirmation, MockGiftcardDb giftcardDb) {
      VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(confirmation.getRequestId());
      if (voidRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(confirmation)).build();
      } else if (!voidRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(confirmation)).build();
      } else if (voidRecord.getVoidResponse() == null) {
         // means the original void failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(confirmation)).build();
      }
      if (voidRecord.getState() == State.REVERSED) {
         VoidReversalRecord reversalRecord =
               giftcardDb.getVoidReversalsTable().getRecord(voidRecord.getLastReversalId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestReversed(confirmation, reversalRecord))
               .build();
      }
      return null;
   }
}
