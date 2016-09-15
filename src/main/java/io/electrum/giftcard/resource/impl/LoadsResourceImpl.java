package io.electrum.giftcard.resource.impl;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.LoadsResource;
import io.electrum.giftcard.api.ILoadsResource;
import io.electrum.giftcard.api.model.LoadConfirmation;
import io.electrum.giftcard.api.model.LoadRequest;
import io.electrum.giftcard.api.model.LoadReversal;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path("/giftcard/v2/loads")
@Api(description = "the Giftcard API", authorizations = { @Authorization("httpBasic") })
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
         UUID requestId,
         UUID confirmationId,
         LoadConfirmation confirmation,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            GiftcardMessageHandlerFactory.getConfirmLoadHandler()
                  .handle(requestId, confirmationId, confirmation, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response load(
         UUID requestId,
         @Valid LoadRequest request,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), request));
      Response rsp =
            GiftcardMessageHandlerFactory.getLoadHandler().handle(requestId, request, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response reverseLoad(
         UUID requestId,
         UUID reversalId,
         LoadReversal reversal,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            GiftcardMessageHandlerFactory.getReverseLoadHandler()
                  .handle(requestId, reversalId, reversal, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }
}
