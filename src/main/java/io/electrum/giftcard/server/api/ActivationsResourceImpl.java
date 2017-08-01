package io.electrum.giftcard.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.ActivationsResource;
import io.electrum.giftcard.api.IActivationsResource;
import io.electrum.giftcard.api.model.ActivationConfirmation;
import io.electrum.giftcard.api.model.ActivationRequest;
import io.electrum.giftcard.api.model.ActivationReversal;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;

@Path("/giftcard/v3/activations")
@Api(description = "the Giftcard API")
public class ActivationsResourceImpl extends ActivationsResource implements IActivationsResource {

   static ActivationsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   @Override
   protected IActivationsResource getResourceImplementation() {
      if (instance == null) {
         instance = new ActivationsResourceImpl();
      }
      return instance;
   }

   @Override
   public Response confirmActivation(
         String requestId,
         String confirmationId,
         ActivationConfirmation confirmation,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            GiftcardMessageHandlerFactory.getConfirmActivationHandler()
                  .handle(requestId, confirmationId, confirmation, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }

   @Override
   public Response activate(
         String requestId,
         @Valid ActivationRequest activationRequest,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), activationRequest));
      Response rsp =
            GiftcardMessageHandlerFactory.getActivationHandler()
                  .handle(requestId, activationRequest, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }

   @Override
   public Response reverseActivation(
         String requestId,
         String reversalId,
         ActivationReversal reversal,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            GiftcardMessageHandlerFactory.getReverseActivationHandler()
                  .handle(requestId, reversalId, reversal, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }
}
