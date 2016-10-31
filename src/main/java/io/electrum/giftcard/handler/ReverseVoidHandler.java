package io.electrum.giftcard.handler;

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
import io.electrum.vas.model.BasicAdviceResponse;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReverseVoidHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(String requestId, String reversalId, VoidReversal reversal, HttpHeaders httpHeaders) {
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
         if (giftcardDb.doesUuidExist(reversalId)) {
            return Response.status(400).entity(GiftcardModelUtils.duplicateRequest(reversal, reversalId)).build();
         }
         VoidReversalsTable voidReversalsTable = giftcardDb.getVoidReversalsTable();
         VoidReversalRecord voidReversalRecord = new VoidReversalRecord(reversalId);
         voidReversalRecord.setRequestId(requestId);
         voidReversalRecord.setVoidReversal(reversal);
         voidReversalsTable.putRecord(voidReversalRecord);
         VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(requestId);
         if (voidRecord != null) {
            voidRecord.addReversalId(reversal.getId());
         }
         // process request
         rsp = canReverseVoid(reversal, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         BasicAdviceResponse adviceResponse = reverseVoid(giftcardDb, reversal);
         // respond
         return Response.accepted().entity(adviceResponse).build();
      } catch (Exception e) {
         log.debug("error processing VoidReversal", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private Response canReverseVoid(VoidReversal reversal, MockGiftcardDb giftcardDb) {
      VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(reversal.getRequestId());
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
         return Response.status(400).entity(GiftcardModelUtils.cardIsVoided(reversal, cardRecord, voidRecord)).build();
      default:
         break;
      }
      return null;
   }

   private BasicAdviceResponse reverseVoid(MockGiftcardDb giftcardDb, VoidReversal reversal) {
      String requestId = reversal.getRequestId();
      VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(requestId);
      if (voidRecord != null) {
         voidRecord.setState(State.REVERSED);
         CardRecord cardRecord = giftcardDb.getCardTable().getRecord(voidRecord.getVoidRequest().getCard().getPan());
         cardRecord.setStatus(Status.ACTIVATED_CONFIRMED);
         cardRecord.setProductId(giftcardDb.getActivationsTable()
               .getRecord(cardRecord.getActivationId())
               .getActivationRequest()
               .getProduct()
               .getId());
      }

      return new BasicAdviceResponse().id(reversal.getId())
            .requestId(reversal.getRequestId())
            .time(reversal.getTime())
            .transactionIdentifiers(reversal.getThirdPartyIdentifiers());
   }
}
