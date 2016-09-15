package io.electrum.giftcard.resource.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.ILookupGiftcardsResource;
import io.electrum.giftcard.api.LookupGiftcardsResource;
import io.electrum.giftcard.api.model.LookupRequest;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path("/giftcard/v2/lookupGiftcard")
@Api(description = "the Giftcard API", authorizations = { @Authorization("httpBasic") })
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
         UUID lookupId,
         LookupRequest body,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp =
            GiftcardMessageHandlerFactory.getLookupGiftcardHandler().handle(lookupId, body, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }
}
