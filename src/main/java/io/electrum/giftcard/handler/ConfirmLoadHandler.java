package io.electrum.giftcard.handler;

import io.electrum.giftcard.api.model.LoadConfirmation;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.LoadConfirmationRecord;
import io.electrum.giftcard.server.backend.records.LoadRecord;
import io.electrum.giftcard.server.backend.records.LoadReversalRecord;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.tables.LoadConfirmationsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.LedgerAmount;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfirmLoadHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(
         String requestId,
         String confirmationId,
         LoadConfirmation confirmation,
         HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateLoadConfirmation(confirmation);
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
         LoadConfirmationsTable loadConfirmationsTable = giftcardDb.getLoadConfirmationsTable();
         LoadConfirmationRecord loadConfirmationRecord = new LoadConfirmationRecord(confirmationId);
         loadConfirmationRecord.setRequestId(requestId);
         loadConfirmationRecord.setLoadConfirmation(confirmation);
         loadConfirmationsTable.putRecord(loadConfirmationRecord);
         LoadRecord loadRecord = giftcardDb.getLoadsTable().getRecord(requestId);
         if (loadRecord != null) {
            loadRecord.addConfirmationId(confirmation.getId());
         }
         // process request
         rsp = canConfirmLoad(confirmation, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         confirmLoad(confirmation, giftcardDb);
         // respond
         return Response.accepted().entity(GiftcardModelUtils.loadConfirmResponse(confirmation)).build();
      } catch (Exception e) {
         log.debug("error processing LoadConfirmation", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void confirmLoad(LoadConfirmation confirmation, MockGiftcardDb giftcardDb) {
      LoadRecord loadRecord = giftcardDb.getLoadsTable().getRecord(confirmation.getRequestId());
      LedgerAmount loadAmount = loadRecord.getLoadResponse().getAmounts().getApprovedAmount();
      loadRecord.addConfirmationId(confirmation.getId());
      loadRecord.setState(State.CONFIRMED);
      CardRecord cardRecord = giftcardDb.getCardRecord(loadRecord.getLoadRequest().getCard());
      LedgerAmount availableBalance = cardRecord.getAvailableBalance();
      // this is the point of a confirmation - the available balance is only updated upon confirmation
      availableBalance.setAmount(availableBalance.getAmount() + loadAmount.getAmount());
   }

   private Response canConfirmLoad(LoadConfirmation confirmation, MockGiftcardDb giftcardDb) {
      LoadRecord loadRecord = giftcardDb.getLoadsTable().getRecord(confirmation.getRequestId());
      if (loadRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(confirmation)).build();
      } else if (!loadRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(confirmation)).build();
      } else if (loadRecord.getLoadResponse() == null) {
         // means the original load failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(confirmation)).build();
      }
      if (loadRecord.getState() == State.REVERSED) {
         LoadReversalRecord reversalRecord =
               giftcardDb.getLoadReversalsTable().getRecord(loadRecord.getLastReversalId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestReversed(confirmation, reversalRecord))
               .build();
      }
      return null;
   }
}
