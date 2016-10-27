package io.electrum.giftcard.handler;

import io.electrum.giftcard.api.model.RedemptionConfirmation;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.RedemptionConfirmationRecord;
import io.electrum.giftcard.server.backend.records.RedemptionRecord;
import io.electrum.giftcard.server.backend.records.RedemptionReversalRecord;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.tables.RedemptionConfirmationsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.BasicAdviceResponse;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmRedemptionHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(
         String requestId,
         String confirmationId,
         RedemptionConfirmation confirmation,
         HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateRedemptionConfirmation(confirmation);
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
         RedemptionConfirmationsTable redemptionConfirmationsTable = giftcardDb.getRedemptionConfirmationsTable();
         RedemptionConfirmationRecord redemptionConfirmationRecord = new RedemptionConfirmationRecord(confirmationId);
         redemptionConfirmationRecord.setRequestId(requestId);
         redemptionConfirmationRecord.setRedemptionConfirmation(confirmation);
         redemptionConfirmationsTable.putRecord(redemptionConfirmationRecord);
         RedemptionRecord redemptionRecord = giftcardDb.getRedemptionsTable().getRecord(requestId);
         if (redemptionRecord != null) {
            redemptionRecord.addConfirmationId(confirmation.getId());
         }
         // process request
         rsp = canConfirmRedemption(confirmation, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         BasicAdviceResponse adviceResponse = confirmRedemption(confirmation, giftcardDb);
         // respond
         return Response.accepted().entity(adviceResponse).build();
      } catch (Exception e) {
         log.debug("error processing RedemptionConfirmation", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private BasicAdviceResponse confirmRedemption(RedemptionConfirmation confirmation, MockGiftcardDb giftcardDb) {
      RedemptionRecord redemptionRecord = giftcardDb.getRedemptionsTable().getRecord(confirmation.getRequestId());
      redemptionRecord.setState(State.CONFIRMED);

      return new BasicAdviceResponse().id(confirmation.getId())
            .requestId(confirmation.getRequestId())
            .time(confirmation.getTime())
            .transactionIdentifiers(confirmation.getThirdPartyIdentifiers());
   }

   private Response canConfirmRedemption(RedemptionConfirmation confirmation, MockGiftcardDb giftcardDb) {
      RedemptionRecord redemptionRecord = giftcardDb.getRedemptionsTable().getRecord(confirmation.getRequestId());
      if (redemptionRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(confirmation)).build();
      } else if (!redemptionRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(confirmation)).build();
      } else if (redemptionRecord.getRedemptionResponse() == null) {
         // means the original redemption failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(confirmation)).build();
      }
      if (redemptionRecord.getState() == State.REVERSED) {
         RedemptionReversalRecord reversalRecord =
               giftcardDb.getRedemptionReversalsTable().getRecord(redemptionRecord.getLastReversalId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestReversed(confirmation, reversalRecord))
               .build();
      }
      return null;
   }
}
