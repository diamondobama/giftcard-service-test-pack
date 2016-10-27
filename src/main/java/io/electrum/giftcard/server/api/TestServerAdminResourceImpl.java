package io.electrum.giftcard.server.api;

import io.electrum.giftcard.handler.GiftcardMessageHandlerFactory;
import io.electrum.giftcard.server.api.model.DataResponse;
import io.electrum.giftcard.server.api.model.ResetRequest;
import io.electrum.giftcard.server.api.model.ResetResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/giftcard/v2/testServerAdmin")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@Api(description = "the Giftcard API")
public class TestServerAdminResourceImpl {

   static TestServerAdminResourceImpl instance = null;
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   protected TestServerAdminResourceImpl getResourceImplementation() {
      if (instance == null) {
         instance = new TestServerAdminResourceImpl();
      }
      return instance;
   }

   @POST
   @Path("/reset")
   @Produces({ "application/json" })
   @ApiOperation(value = "Reset the test data in the Giftcard Test Server.", notes = "The Test Server Admin Reset endpoint allows a user of the Test Server "
         + "to reset the test data in the Test Server's database. This means that "
         + "all card and product data will be reset to initial settings and ALL "
         + "message data will be lost. This operation affects all data used by the "
         + "user identified by the HTTP Basic Auth username and password "
         + "combination. <em>This cannot be reversed.</em>", authorizations = { @Authorization(value = "httpBasic") }, tags = { "Test Server Admin", })
   @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = ResetResponse.class),
         @ApiResponse(code = 400, message = "Bad Request", response = ResetResponse.class),
         @ApiResponse(code = 500, message = "Internal Server Error", response = ResetResponse.class) })
   public void reset(
         @ApiParam(value = "The activation confirmation information.", required = true) ResetRequest body,
         @Context SecurityContext securityContext,
         @Suspended AsyncResponse asyncResponse,
         @Context HttpHeaders httpHeaders,
         @Context UriInfo uriInfo,
         @Context HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s\n%s", httpServletRequest.getMethod(), uriInfo.getPath(), body));
      Response rsp = GiftcardMessageHandlerFactory.getResetHandler().handle(body, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      asyncResponse.resume(rsp);
   }

   @GET
   @Path("/data")
   @Produces({ "application/json" })
   @ApiOperation(value = "Retrieve the test data confirgured in the Giftcard Test Server.", notes = "The Test Server Admin Data endpoint allows a user of the Test Server "
         + "to retrieve all the card and product data currently configured in the Test "
         + "Server. This allows the user to obtain valid card and product information "
         + "to use for testing. Note that requests submitted which use cards or "
         + "products different from those returned by this operations will not be "
         + "recognised by the Test Server and will lead to an error response from the " + "Test Server.", tags = { "Test Server Admin", })
   @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = DataResponse.class),
         @ApiResponse(code = 500, message = "Internal Server Error") })
   public void data(
         @Context SecurityContext securityContext,
         @Suspended AsyncResponse asyncResponse,
         @Context HttpHeaders httpHeaders,
         @Context UriInfo uriInfo,
         @Context HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      Response rsp = GiftcardMessageHandlerFactory.getDataHandler().handle(httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      asyncResponse.resume(rsp);
   }

   @GET
   @Path("/data/card/{cardNumber}")
   @Produces({ "application/json" })
   @ApiOperation(value = "Retrieve the test data configured in the Giftcard Test Server for a certain card.", notes = "The Test "
         + "Server Admin Data endpoint allows a user of the Test Server to retrieve "
         + "the card data and state for a card configured in the Test Server. This "
         + "allows the user to obtain valid card information to use for testing as "
         + "well as the opportunity to examine the state of the card as it is 'used' " + "in a test.", authorizations = { @Authorization(value = "httpBasic") }, tags = { "Test Server Admin", })
   @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = DataResponse.class),
         @ApiResponse(code = 500, message = "Internal Server Error") })
   public void singleCardData(
         @ApiParam(value = "The PAN of the card for which data should be retrieved.", required = true) @PathParam("cardNumber") String cardNumber,
         @Context SecurityContext securityContext,
         @Suspended AsyncResponse asyncResponse,
         @Context HttpHeaders httpHeaders,
         @Context UriInfo uriInfo,
         @Context HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      Response rsp = GiftcardMessageHandlerFactory.getDataHandler().handleCardRequest(cardNumber, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      asyncResponse.resume(rsp);
   }

   @GET
   @Path("/data/product/{productId}")
   @Produces({ "application/json" })
   @ApiOperation(value = "Retrieve the test data configured in the Giftcard Test Server for a certain product.", notes = "The Test "
         + "Server Admin Data endpoint allows a user of the Test Server to retrieve "
         + "the product data currently configured in the Test Server for a single "
         + "specified product. This allows the user to obtain valid product information " + "to use for testing.", authorizations = { @Authorization(value = "httpBasic") }, tags = { "Test Server Admin", })
   @ApiResponses(value = { @ApiResponse(code = 200, message = "OK", response = DataResponse.class),
         @ApiResponse(code = 500, message = "Internal Server Error") })
   public void singleProductData(
         @ApiParam(value = "The ID of the product for which data should be retrieved.", required = true) @PathParam("productId") String productId,
         @Context SecurityContext securityContext,
         @Suspended AsyncResponse asyncResponse,
         @Context HttpHeaders httpHeaders,
         @Context UriInfo uriInfo,
         @Context HttpServletRequest httpServletRequest) {
      log.info(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      log.debug(String.format("%s %s", httpServletRequest.getMethod(), uriInfo.getPath()));
      Response rsp =
            GiftcardMessageHandlerFactory.getDataHandler().handleProductRequest(productId, httpHeaders, uriInfo);
      log.debug(String.format("Entity returned:\n%s", rsp.getEntity()));
      asyncResponse.resume(rsp);
   }
}
