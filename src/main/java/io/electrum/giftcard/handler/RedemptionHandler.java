package io.electrum.giftcard.handler;

import io.electrum.giftcard.api.model.ErrorDetail;
import io.electrum.giftcard.api.model.RedemptionRequest;
import io.electrum.giftcard.api.model.RedemptionResponse;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.ActivationRecord;
import io.electrum.giftcard.server.backend.records.ActivationReversalRecord;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.RedemptionRecord;
import io.electrum.giftcard.server.backend.records.VoidRecord;
import io.electrum.giftcard.server.backend.tables.RedemptionsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.LedgerAmount;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedemptionHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(String requestId, RedemptionRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      RedemptionRecord redemptionRecord = null;
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateRedemptionRequest(request);
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
         if (giftcardDb.doesUuidExist(requestId)) {
            return Response.status(400).entity(GiftcardModelUtils.duplicateRequest(request, requestId)).build();
         }
         // record request
         RedemptionsTable redemptionsTable = giftcardDb.getRedemptionsTable();
         redemptionRecord = new RedemptionRecord(requestId);
         redemptionRecord.setRedemptionRequest(request);
         redemptionsTable.putRecord(redemptionRecord);
         // check card can be activated
         rsp = canRedeemCard(giftcardDb, request);
         if (rsp != null) {
            return rsp;
         }
         // do the redemption
         ErrorDetail redemptionError = redeem(request, giftcardDb);
         if (redemptionError != null) {
            rsp = Response.status(400).entity(redemptionError).build();
            return rsp;
         }
         // record response
         RedemptionResponse redemptionRsp = GiftcardModelUtils.redemptionRspFromReq(giftcardDb, request);
         redemptionRecord.setRedemptionResponse(redemptionRsp);
         // respond
         return Response.status(201).entity(redemptionRsp).build();
      } catch (Exception e) {
         log.debug("error processing RedemptionRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(GiftcardModelUtils.exceptionResponse(request)).build();
         return rsp;
      } finally {
         if (redemptionRecord != null) {
            redemptionRecord.setResponded();
         }
      }
   }

   private Response canRedeemCard(MockGiftcardDb giftcardDb, RedemptionRequest request) {
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
               .entity(
                     GiftcardModelUtils.cardIsNotYetActive(
                           request,
                           cardRecord,
                           activationRecord,
                           activationReversalRecord))
               .build();
      case ACTIVATED_CONFIRMED:
         break;
      case VOIDED:
      case VOIDED_CONFIRMED:
         VoidRecord voidRecord = giftcardDb.getVoidsTable().getRecord(cardRecord.getVoidId());
         return Response.status(400).entity(GiftcardModelUtils.cardIsVoided(request, cardRecord, voidRecord)).build();
      }
      long requestedAmount = request.getAmounts().getRequestAmount().getAmount();
      long balance = cardRecord.getAvailableBalance().getAmount();
      if (requestedAmount > balance) {
         return Response.status(400).entity(GiftcardModelUtils.insufficientFunds(cardRecord, request)).build();
      }
      String expectedClearPin = cardRecord.getCard().getClearPin();
      if (expectedClearPin == null || expectedClearPin.isEmpty()) {
         expectedClearPin = "";
      }
      String expectedEncryptedPin = cardRecord.getCard().getEncryptedPin();
      if (expectedEncryptedPin == null || expectedEncryptedPin.isEmpty()) {
         expectedEncryptedPin = "";
      }
      String submittedClearPin = request.getCard().getClearPin();
      if (submittedClearPin == null || submittedClearPin.isEmpty()) {
         submittedClearPin = "";
      }
      String submittedEncryptedPin = request.getCard().getEncryptedPin();
      if (submittedEncryptedPin == null || submittedEncryptedPin.isEmpty()) {
         submittedEncryptedPin = "";
      }
      if (!expectedClearPin.equals(submittedClearPin) || !expectedEncryptedPin.equals(submittedEncryptedPin)) {
         return Response.status(400).entity(GiftcardModelUtils.incorrectPin(cardRecord, request)).build();
      }
      if (!cardRecord.expiryDateCorrect(request.getCard().getExpiryDate())) {
         return Response.status(400).entity(GiftcardModelUtils.cardExpiryInvalid(cardRecord, request)).build();
      }
      if (cardRecord.cardExpired()) {
         return Response.status(400).entity(GiftcardModelUtils.cardExpired(request)).build();
      }
      return null;
   }

   private ErrorDetail redeem(RedemptionRequest request, MockGiftcardDb giftcardDb) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      cardRecord.addRedemptionId(request.getId());
      // deduct the funds now in case we wait a long time for the reversal and the customer buys ALL the toys
      LedgerAmount availableBalance = cardRecord.getAvailableBalance();
      availableBalance.amount(availableBalance.getAmount() - request.getAmounts().getRequestAmount().getAmount());
      LedgerAmount bookBalance = cardRecord.getBalance();
      bookBalance.amount(bookBalance.getAmount() - request.getAmounts().getRequestAmount().getAmount());
      return null;
   }
}
