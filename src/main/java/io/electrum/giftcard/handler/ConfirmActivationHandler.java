package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.ActivationConfirmation;
import io.electrum.giftcard.resource.impl.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.ActivationConfirmationRecord;
import io.electrum.giftcard.server.backend.records.ActivationRecord;
import io.electrum.giftcard.server.backend.records.ActivationReversalRecord;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.CardRecord.Status;
import io.electrum.giftcard.server.backend.records.RequestRecord.State;
import io.electrum.giftcard.server.backend.tables.ActivationConfirmationsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;

public class ConfirmActivationHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(
         UUID requestId,
         UUID confirmationId,
         ActivationConfirmation confirmation,
         HttpHeaders httpHeaders) {
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateActivationConfirmation(confirmation);
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
         if (giftcardDb.doesUuidExist(confirmationId.toString())) {
            return Response.status(400).entity(GiftcardModelUtils.duplicateRequest(confirmationId.toString())).build();
         }
         ActivationConfirmationsTable activationConfirmationsTable = giftcardDb.getActivationConfirmationsTable();
         ActivationConfirmationRecord activationConfirmationRecord =
               new ActivationConfirmationRecord(confirmationId.toString());
         activationConfirmationRecord.setRequestId(requestId.toString());
         activationConfirmationRecord.setActivationConfirmation(confirmation);
         activationConfirmationsTable.putRecord(activationConfirmationRecord);
         ActivationRecord activationRecord = giftcardDb.getActivationsTable().getRecord(requestId.toString());
         if (activationRecord != null) {
            activationRecord.addConfirmationId(confirmation.getId().toString());
         }
         // process request
         rsp = canConfirmActivation(confirmation, giftcardDb);
         if (rsp != null) {
            return rsp;
         }
         confirmActivation(confirmation, giftcardDb);
         // respond
         return Response.accepted().build();
      } catch (Exception e) {
         log.debug("error processing ActivationConfirmation", e);
         Response rsp = Response.serverError().entity(e.getMessage()).build();
         return rsp;
      }
   }

   private void confirmActivation(ActivationConfirmation confirmation, MockGiftcardDb giftcardDb) {
      ActivationRecord activationRecord =
            giftcardDb.getActivationsTable().getRecord(confirmation.getRequestId().toString());
      activationRecord.setState(State.CONFIRMED);
      CardRecord cardRecord = giftcardDb.getCardRecord(activationRecord.getActivationRequest().getCard());
      cardRecord.setStatus(Status.ACTIVATED_CONFIRMED);
   }

   private Response canConfirmActivation(ActivationConfirmation confirmation, MockGiftcardDb giftcardDb) {
      ActivationRecord activationRecord =
            giftcardDb.getActivationsTable().getRecord(confirmation.getRequestId().toString());
      if (activationRecord == null) {
         return Response.status(404).entity(GiftcardModelUtils.unableToLocateRecord(confirmation)).build();
      } else if (!activationRecord.isResponded()) {
         // means we're actually still processing the request
         return Response.status(400).entity(GiftcardModelUtils.requestBeingProcessed(confirmation)).build();
      } else if (activationRecord.getActivationResponse() == null) {
         // means the original activation failed.
         return Response.status(400).entity(GiftcardModelUtils.originalRequestFailed(confirmation)).build();
      }
      if (activationRecord.getState() == State.REVERSED) {
         ActivationReversalRecord reversalRecord =
               giftcardDb.getActivationReversalsTable().getRecord(activationRecord.getLastReversalId());
         return Response.status(400)
               .entity(GiftcardModelUtils.originalRequestReversed(confirmation, reversalRecord))
               .build();
      }
      return null;
   }
}
