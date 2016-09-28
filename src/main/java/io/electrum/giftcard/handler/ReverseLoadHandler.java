package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.LoadRequest;
import io.electrum.giftcard.api.model.LoadReversal;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.LoadConfirmationRecord;
import io.electrum.giftcard.server.backend.records.LoadRecord;
import io.electrum.giftcard.server.backend.records.LoadReversalRecord;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.tables.LoadReversalsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.LedgerAmount;

public class ReverseLoadHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, UUID reversalId, LoadReversal reversal, HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateLoadReversal(reversal);
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
         LoadReversalsTable loadReversalsTable = giftcardDb.getLoadReversalsTable();
         LoadReversalRecord loadReversalRecord = new LoadReversalRecord(reversalId.toString());
         loadReversalRecord.setRequestId(requestId.toString());
         loadReversalRecord.setLoadReversal(reversal);
         loadReversalsTable.putRecord(loadReversalRecord);
         LoadRecord loadRecord = giftcardDb.getLoadsTable().getRecord(requestId.toString());
         if (loadRecord != null) {
            loadRecord.addReversalId(reversal.getId().toString());
         }
         // process request
         rsp = canReverseLoad(reversal, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         reverseLoad(reversal, giftcardDb);
         // respond
         return Response.accepted().build();
      } catch (Exception e) {
         log.debug("error processing LoadReversal", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void reverseLoad(LoadReversal reversal, MockGiftcardDb giftcardDb) {
      // nothing actually required to do when reversing a load - just don't update the card record's balance.
      LoadRecord loadRecord = giftcardDb.getLoadsTable().getRecord(reversal.getRequestId().toString());
      loadRecord.setState(State.REVERSED);
      LoadRequest loadRequest = loadRecord.getLoadRequest();
      CardRecord cardRecord = giftcardDb.getCardRecord(loadRequest.getCard());
      Long loadAmount = loadRequest.getAmounts().getApprovedAmount().getAmount();
      LedgerAmount balance = cardRecord.getBalance();
      //this is the actual reverse step here
      balance.setAmount(balance.getAmount() - loadAmount);
   }

   private Response canReverseLoad(LoadReversal reversal, MockGiftcardDb giftcardDb) {
      LoadRecord loadRecord = giftcardDb.getLoadsTable().getRecord(reversal.getRequestId().toString());
      if (loadRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(reversal)).build();
      } else if (!loadRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(reversal)).build();
      } else if (loadRecord.getLoadResponse() == null) {
         // means the original activation failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(reversal)).build();
      }
      if (loadRecord.getState() == State.CONFIRMED) {
         LoadConfirmationRecord confirmationRecord =
               giftcardDb.getLoadConfirmationsTable().getRecord(loadRecord.getLastConfirmationId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestConfirmed(reversal, confirmationRecord))
               .build();
      }
      CardRecord cardRecord = giftcardDb.getCardRecord(loadRecord.getLoadRequest().getCard());
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
