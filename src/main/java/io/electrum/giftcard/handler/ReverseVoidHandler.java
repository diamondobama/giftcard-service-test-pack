package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.VoidReversal;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.CardRecord.Status;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.records.VoidConfirmationRecord;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.records.VoidReversalRecord;
import io.electrum.giftcard.server.backend.tables.VoidReversalsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;

public class ReverseVoidHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, UUID reversalId, VoidReversal reversal, HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateVoidReversal(reversal);
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
         VoidReversalsTable voidReversalsTable = giftcardDb.getVoidReversalsTable();
         VoidReversalRecord voidReversalRecord = new VoidReversalRecord(reversalId.toString());
         voidReversalRecord.setRequestId(requestId.toString());
         voidReversalRecord.setVoidReversal(reversal);
         voidReversalsTable.putRecord(voidReversalRecord);
         VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(requestId.toString());
         if (voidRecord != null) {
            voidRecord.addReversalId(reversal.getId().toString());
         }
         // process request
         rsp = canReverseVoid(reversal, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         reverseVoid(giftcardDb, reversal);
         // respond
         return Response.accepted().build();
      } catch (Exception e) {
         log.debug("error processing VoidReversal", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private Response canReverseVoid(VoidReversal reversal, MockGiftcardDb giftcardDb) {
      VoidRecord voidRecord =
            giftcardDb.getVoidsTable().getRecord(reversal.getRequestId().toString());
      if (voidRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(reversal)).build();
      } else if (!voidRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(reversal)).build();
      } else if (voidRecord.getVoidResponse() == null) {
         // means the original activation failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(reversal)).build();
      }
      if (voidRecord.getState() == State.CONFIRMED) {
         VoidConfirmationRecord confirmationRecord =
               giftcardDb.getVoidConfirmationsTable().getRecord(voidRecord.getLastConfirmationId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestConfirmed(reversal, confirmationRecord))
               .build();
      }
      CardRecord cardRecord = giftcardDb.getCardRecord(voidRecord.getVoidRequest().getCard());
      switch (cardRecord.getStatus()) {
      case ACTIVATED_CONFIRMED:
      case VOIDED:
         break;
      case VOIDED_CONFIRMED:
         return Response.status(400).entity(GiftcardModelUtils.cardIsVoided(cardRecord, voidRecord)).build();
      default:
         break;
      }
      return null;
   }

   private void reverseVoid(MockGiftcardDb giftcardDb, VoidReversal reversal) {
      String requestId = reversal.getRequestId().toString();
      VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(requestId);
      if(voidRecord != null)
      {
         voidRecord.setState(State.REVERSED);
         CardRecord cardRecord = giftcardDb.getCardTable().getRecord(voidRecord.getVoidRequest().getCard().getPan());
         cardRecord.setStatus(Status.ACTIVATED_CONFIRMED);
         cardRecord.setProductId(giftcardDb.getActivationsTable().getRecord(cardRecord.getActivationId()).getActivationRequest().getProduct().getId());
      }
   }
}
