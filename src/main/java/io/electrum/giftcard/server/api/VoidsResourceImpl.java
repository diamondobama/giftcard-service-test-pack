package io.electrum.giftcard.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.IVoidsResource;
import io.electrum.giftcard.api.VoidsResource;
import io.electrum.giftcard.api.model.VoidConfirmation;
import io.electrum.giftcard.api.model.VoidRequest;
import io.electrum.giftcard.api.model.VoidReversal;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;

@Path("/giftcard/v3/voids")
@Api(description = "the Giftcard API")
public class VoidsResourceImpl extends VoidsResource implements IVoidsResource {

   static VoidsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   @Override
   protected IVoidsResource getResourceImplementation() {
      if (instance == null) {
         instance = new VoidsResourceImpl();
      }
      return instance;
   }

   @Override
   public Response confirmVoid(
         String requestId,
         String confirmationId,
         VoidConfirmation confirmation,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            GiftcardMessageHandlerFactory.getConfirmVoidHandler()
                  .handle(requestId, confirmationId, confirmation, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }

   @Override
   public Response voidGiftcard(
         String requestId,
         @Valid VoidRequest voidRequest,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), voidRequest));
      Response rsp =
            GiftcardMessageHandlerFactory.getVoidHandler().handle(requestId, voidRequest, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }

   @Override
   public Response reverseVoid(
         String requestId,
         String reversalId,
         VoidReversal reversal,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            GiftcardMessageHandlerFactory.getReverseVoidHandler().handle(requestId, reversalId, reversal, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }
}
