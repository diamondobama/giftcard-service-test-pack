package io.electrum.giftcard.handler;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.api.model.CardData;
import io.electrum.giftcard.server.api.model.DataResponse;
import io.electrum.giftcard.server.api.model.ProductData;
import io.electrum.giftcard.server.backend.db.MockGiftcardDb;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.ProductRecord;
import io.electrum.giftcard.server.backend.tables.ProductTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;

public class DataHandler {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class.getPackage().getName());

   public Response handle(HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         List<CardData> cards = new ArrayList<CardData>();
         List<ProductData> products = new ArrayList<ProductData>();
         DataResponse dataResponse = new DataResponse().cards(cards).products(products);
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         Enumeration<CardRecord> cardRecords = giftcardDb.getCardTable().getRecords();
         ProductTable productTable = giftcardDb.getProductTable();
         while (cardRecords.hasMoreElements()) {
            CardRecord cardRecord = cardRecords.nextElement();
            ProductRecord productRecord = productTable.getRecord(cardRecord.getProductId());
            cards.add(
                  new CardData().card(cardRecord.getCard())
                        .balance(cardRecord.getBalance())
                        .status(cardRecord.getStatus())
                        .product(productRecord == null ? null : productRecord.getProduct()));
         }
         Enumeration<ProductRecord> productRecords = productTable.getRecords();
         while (productRecords.hasMoreElements()) {
            ProductRecord productRecord = productRecords.nextElement();
            products.add(
                  new ProductData().product(productRecord.getProduct())
                        .startingBalance(productRecord.getStartingBalance()));
         }
         return Response.status(200).entity(dataResponse).build();
      } catch (Exception e) {
         log.debug("error processing data request", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }

   public Response handleCardRequest(String cardNumber, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         List<CardData> cards = new ArrayList<CardData>();
         List<ProductData> products = new ArrayList<ProductData>();
         DataResponse dataResponse = new DataResponse().cards(cards).products(products);
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         CardRecord cardRecord = giftcardDb.getCardTable().getRecord(cardNumber);
         ProductTable productTable = giftcardDb.getProductTable();
         if (cardRecord != null) {
            ProductRecord productRecord = productTable.getRecord(cardRecord.getProductId());
            cards.add(
                  new CardData().card(cardRecord.getCard())
                        .balance(cardRecord.getBalance())
                        .status(cardRecord.getStatus())
                        .product(productRecord == null ? null : productRecord.getProduct()));
         }
         return Response.status(200).entity(dataResponse).build();
      } catch (Exception e) {
         log.debug("error processing data request", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }

   public Response handleProductRequest(String productId, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         List<CardData> cards = new ArrayList<CardData>();
         List<ProductData> products = new ArrayList<ProductData>();
         DataResponse dataResponse = new DataResponse().cards(cards).products(products);
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         ProductRecord productRecord = giftcardDb.getProductTable().getRecord(productId);
         if (productRecord != null) {
            products.add(
                  new ProductData().product(productRecord.getProduct())
                        .startingBalance(productRecord.getStartingBalance()));
         }
         return Response.status(200).entity(dataResponse).build();
      } catch (Exception e) {
         log.debug("error processing data request", e);
         for (StackTraceElement ste : e.getStackTrace()) {
            log.debug(ste.toString());
         }
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }
}
