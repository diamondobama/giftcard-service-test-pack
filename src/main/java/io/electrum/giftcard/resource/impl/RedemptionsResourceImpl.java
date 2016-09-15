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

import io.electrum.giftcard.api.RedemptionsResource;
import io.electrum.giftcard.api.IRedemptionsResource;
import io.electrum.giftcard.api.model.RedemptionConfirmation;
import io.electrum.giftcard.api.model.RedemptionRequest;
import io.electrum.giftcard.api.model.RedemptionReversal;
import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.Authorization;

@Path("/giftcard/v2/redemptions")
@Api(description = "the Giftcard API", authorizations = { @Authorization("httpBasic") })
public class RedemptionsResourceImpl extends RedemptionsResource implements IRedemptionsResource {

   static RedemptionsResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   @Override
   protected IRedemptionsResource getResourceImplementation() {
      if (instance == null) {
         instance = new RedemptionsResourceImpl();
      }
      return instance;
   }

   @Override
   public Response confirmRedemption(
         UUID requestId,
         UUID confirmationId,
         RedemptionConfirmation confirmation,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), confirmation));
      Response rsp =
            GiftcardMessageHandlerFactory.getConfirmRedemptionHandler()
                  .handle(requestId, confirmationId, confirmation, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response redeem(
         UUID requestId,
         @Valid RedemptionRequest request,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), request));
      Response rsp =
            GiftcardMessageHandlerFactory.getRedemptionHandler().handle(requestId, request, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }

   @Override
   public Response reverseRedemption(
         UUID requestId,
         UUID reversalId,
         RedemptionReversal reversal,
         SecurityContext securityContext,
         HttpHeaders httpHeaders,
         UriInfo uriInfo,
         HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), reversal));
      Response rsp =
            GiftcardMessageHandlerFactory.getReverseRedemptionHandler()
                  .handle(requestId, reversalId, reversal, httpHeaders);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      return rsp;
   }
}
