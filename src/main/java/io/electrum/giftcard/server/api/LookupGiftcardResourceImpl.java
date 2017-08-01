package io.electrum.giftcard.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.ILookupGiftcardsResource;
import io.electrum.giftcard.api.LookupGiftcardsResource;
import io.electrum.giftcard.api.model.LookupRequest;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;

@Path("/giftcard/v3/lookupGiftcard")
@Api(description = "the Giftcard API")
public class LookupGiftcardResourceImpl extends LookupGiftcardsResource implements ILookupGiftcardsResource {

   static LookupGiftcardResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   @Override
   protected ILookupGiftcardsResource getResourceImplementation() {
      if (instance == null) {
         instance = new LookupGiftcardResourceImpl();
      }
      return instance;
   }

   @Override
   public Response lookupGiftcard(
         String lookupId,
         LookupRequest body,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            GiftcardMessageHandlerFactory.getLookupGiftcardHandler().handle(lookupId, body, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }
}
