package io.electrum.giftcard.handler;

import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.LookupRequest;
import io.electrum.giftcard.api.model.LookupResponse;
import io.electrum.giftcard.resource.impl.GiftcardTestServer;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.LookupRecord;
import io.electrum.giftcard.server.backend.tables.LookupsTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;

public class LookupGiftcardHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(UUID requestId, LookupRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      LookupRecord lookupRecord = null;
      try {
         // check its a valid request
         Response rsp = GiftcardModelUtils.validateLookupRequest(request);
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
         LookupsTable lookupsTable = giftcardDb.getLookupsTable();
         lookupRecord = new LookupRecord(requestId.toString());
         lookupRecord.setLookupRequest(request);
         lookupsTable.putRecord(lookupRecord);
         // check card can be activated
         rsp = canLookupCard(giftcardDb, request);
         if (rsp != null) {
            return rsp;
         }
         // nothing to do for the lookup
         // record response
         LookupResponse lookupRsp = GiftcardModelUtils.lookupRspFromReq(giftcardDb, request);
         lookupRecord.setLookupResponse(lookupRsp);
         // respond
         return Response.status(201).entity(lookupRsp).build();
      } catch (Exception e) {
         log.debug("error processing LookupRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(GiftcardModelUtils.exceptionResponse()).build();
         return rsp;
      } finally {
         if (lookupRecord != null) {
            lookupRecord.setResponded();
         }
      }
   }

   private Response canLookupCard(MockGiftcardDb giftcardDb, LookupRequest request) {
      CardRecord cardRecord = giftcardDb.getCardRecord(request.getCard());
      if (cardRecord == null) {
         return Response.status(400).entity(GiftcardModelUtils.cardNotFound(request)).build();
      }
      return null;
   }
}
