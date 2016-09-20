package io.electrum.giftcard.handler;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.api.model.ResetRequest;
import io.electrum.giftcard.server.api.model.ResetResponse;
import io.electrum.giftcard.server.api.model.ResetRequest.Acknowledgments;
import io.electrum.giftcard.server.api.model.ResetRequest.Declarations;
import io.electrum.giftcard.server.api.model.ResetResponse.Outcomes;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.util.GiftcardModelUtils;

public class ResetHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(ResetRequest request, HttpHeaders httpHeaders, UriInfo uriInfo) {
      if(request == null)
      {
         return Response.status(400).entity(new ResetResponse().outcome(Outcomes.EMPTY_REQUEST)).build();
      }
      try {
         if(request.getAcknowledgement() != Acknowledgments.TRUE)
         {
            return Response.status(400).entity(new ResetResponse().outcome(Outcomes.NO_ACK)).build();
         }
         if(request.getDeclaration() != Declarations.TRUE)
         {
            return Response.status(400).entity(new ResetResponse().outcome(Outcomes.NO_DEC)).build();
         }
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         if(GiftcardTestServer.getBackend().doesDbForUserExist(username, password))
         {
            // get the DB for this user
            MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
            giftcardDb.reset();
         }
         else
         {
            return Response.status(400).entity(new ResetResponse().outcome(Outcomes.UNKNOWN_USER)).build();
         }
         return Response.status(200).entity(new ResetResponse().outcome(Outcomes.SUCCESSFUL)).build();
      } catch (Exception e) {
         log.debug("error processing ResetRequest", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().entity(new ResetResponse().outcome(Outcomes.SERVER_ERROR)).build();
         return rsp;
      }
   }
}
