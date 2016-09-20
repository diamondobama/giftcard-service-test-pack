package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.RedemptionRequest;
import io.electrum.giftcard.api.model.RedemptionReversal;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.RedemptionConfirmationRecord;
import io.electrum.giftcard.server.backend.records.RedemptionRecord;
import io.electrum.giftcard.server.backend.records.RedemptionReversalRecord;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.tables.RedemptionReversalsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.LedgerAmount;

public class ReverseRedemptionHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, UUID reversalId, RedemptionReversal reversal, HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateRedemptionReversal(reversal);
         if (rsp != null) {
            return rsp;
         }
         rsp = GiftcardModelUtils.isUuidConsistent(requestId, reversalId, reversal);
         if (rsp != null) {
            return rsp;
         }
         // get the DB for this user
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         // record request
         if (giftcardDb.doesUuidExist(reversalId.toString())) {
            return Response.status(400).entity(GiftcardModelUtils.duplicateRequest(reversalId.toString())).build();
         }
         RedemptionReversalsTable redemptionReversalsTable = giftcardDb.getRedemptionReversalsTable();
         RedemptionReversalRecord redemptionReversalRecord = new RedemptionReversalRecord(reversalId.toString());
         redemptionReversalRecord.setRequestId(requestId.toString());
         redemptionReversalRecord.setRedemptionReversal(reversal);
         redemptionReversalsTable.putRecord(redemptionReversalRecord);
         RedemptionRecord redemptionRecord = giftcardDb.getRedemptionsTable().getRecord(requestId.toString());
         if (redemptionRecord != null) {
            redemptionRecord.addReversalId(reversal.getId().toString());
         }
         // process request
         rsp = canReverseRedemption(reversal, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         reverseRedemption(reversal, giftcardDb);
         // respond
         return Response.accepted().build();
      } catch (Exception e) {
         log.debug("error processing RedemptionReversal", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void reverseRedemption(RedemptionReversal reversal, MockGiftcardDb giftcardDb) {
      RedemptionRecord redemptionRecord = giftcardDb.getRedemptionsTable().getRecord(reversal.getRequestId().toString());
      RedemptionRequest redemptionRequest = redemptionRecord.getRedemptionRequest();
      CardRecord cardRecord = giftcardDb.getCardRecord(redemptionRequest.getCard());
      LedgerAmount balance = cardRecord.getBalance();
      balance.setAmount(balance.getAmount()+redemptionRequest.getAmounts().getRequestAmount().getAmount());
      redemptionRecord.setState(State.REVERSED);
   }

   private Response canReverseRedemption(RedemptionReversal reversal, MockGiftcardDb giftcardDb) {
      RedemptionRecord redemptionRecord = giftcardDb.getRedemptionsTable().getRecord(reversal.getRequestId().toString());
      if (redemptionRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(reversal)).build();
      } else if (!redemptionRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(reversal)).build();
      } else if (redemptionRecord.getRedemptionResponse() == null) {
         // means the original activation failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(reversal)).build();
      }
      if (redemptionRecord.getState() == State.CONFIRMED) {
         RedemptionConfirmationRecord confirmationRecord =
               giftcardDb.getRedemptionConfirmationsTable().getRecord(redemptionRecord.getLastConfirmationId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestConfirmed(reversal, confirmationRecord))
               .build();
      }
      CardRecord cardRecord = giftcardDb.getCardRecord(redemptionRecord.getRedemptionRequest().getCard());
      switch (cardRecord.getStatus()) {
      case ACTIVATED_CONFIRMED:
         break;
      case VOIDED:
      case VOIDED_CONFIRMED:
         VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(cardRecord.getVoidId());
         return Response.status(400).entity(GiftcardModelUtils.cardIsVoided(cardRecord, voidRecord)).build();
      default:
         // NEW and ACTIVATED status shouldn't occur by now.
         break;
      }
      return null;
   }
}
