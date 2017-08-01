package io.electrum.giftcard.server.api;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.ILoadsResource;
import io.electrum.giftcard.api.LoadsResource;
import io.electrum.giftcard.api.model.LoadConfirmation;
import io.electrum.giftcard.api.model.LoadRequest;
import io.electrum.giftcard.api.model.LoadReversal;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;

@Path("/giftcard/v3/loads")
@Api(description = "the Giftcard API")
public class LoadsResourceImpl extends LoadsResource implements ILoadsResource {

   static LoadsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   @Override
   protected ILoadsResource getResourceImplementation() {
      if (instance == null) {
         instance = new LoadsResourceImpl();
      }
      return instance;
   }

   @Override
   public Response confirmLoad(
         String requestId,
         String confirmationId,
         LoadConfirmation confirmation,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            GiftcardMessageHandlerFactory.getConfirmLoadHandler()
                  .handle(requestId, confirmationId, confirmation, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }

   @Override
   public Response load(
         String requestId,
         @Valid LoadRequest loadRequest,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), loadRequest));
      Response rsp =
            GiftcardMessageHandlerFactory.getLoadHandler().handle(requestId, loadRequest, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }

   @Override
   public Response reverseLoad(
         String requestId,
         String reversalId,
         LoadReversal reversal,
         SecurityContext securityContext,
         Request request,
         HttpHeaders httpHeaders,
         AsyncResponse asyncResponse,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            GiftcardMessageHandlerFactory.getReverseLoadHandler().handle(requestId, reversalId, reversal, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));

      asyncResponse.resume(rsp);

      return rsp;
   }
}
