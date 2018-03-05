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
import io.electrum.giftcard.server.backend.records.AdviceRecord;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.ProductRecord;
import io.electrum.giftcard.server.backend.records.RequestRecord;
import io.electrum.giftcard.server.backend.tables.AdviceTable;
import io.electrum.giftcard.server.backend.tables.CardTable;
import io.electrum.giftcard.server.backend.tables.ProductTable;
import io.electrum.giftcard.server.backend.tables.RequestTable;
import io.electrum.giftcard.server.util.GiftcardModelUtils;
import io.electrum.vas.model.BasicAdvice;

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
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }

   /**
    * Removes a card from the CardTable. This method will also remove any associated activation, load, redeem or void
    * requests and their associated advices. All records pertaining to messages referencing the particular card are
    * removed first and then the CardRecord itself.
    * 
    * @param cardNumber
    * @param httpHeaders
    * @param uriInfo
    * @return
    */
   public Response handleDeleteCardRequest(String cardNumber, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         CardRecord cardRecord = giftcardDb.getCardTable().getRecord(cardNumber);
         if (cardRecord != null) {
            removeMsgRecords(
                  cardRecord.getActivationId(),
                  giftcardDb.getActivationsTable(),
                  giftcardDb.getActivationConfirmationsTable(),
                  giftcardDb.getActivationReversalsTable());
            for (String loadId : cardRecord.getLoadIds()) {
               removeMsgRecords(
                     loadId,
                     giftcardDb.getLoadsTable(),
                     giftcardDb.getLoadConfirmationsTable(),
                     giftcardDb.getLoadReversalsTable());
            }
            for (String redeemId : cardRecord.getRedemptionIds()) {
               removeMsgRecords(
                     redeemId,
                     giftcardDb.getRedemptionsTable(),
                     giftcardDb.getRedemptionConfirmationsTable(),
                     giftcardDb.getRedemptionReversalsTable());
            }
            removeMsgRecords(
                  cardRecord.getVoidId(),
                  giftcardDb.getVoidsTable(),
                  giftcardDb.getVoidConfirmationsTable(),
                  giftcardDb.getVoidReversalsTable());
            // Remove the actual CardRecord now
            giftcardDb.getCardTable().removeRecord(cardNumber);
         }
         DataResponse dataResponse = (DataResponse) handle(httpHeaders, uriInfo).getEntity();
         return Response.status(200).entity(dataResponse).build();
      } catch (Exception e) {
         log.debug("error processing data request", e);
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }

   public void removeMsgRecords(
         String requestId,
         RequestTable<? extends RequestRecord> requestTable,
         AdviceTable<? extends AdviceRecord<? extends BasicAdvice>> confirmationsTable,
         AdviceTable<? extends AdviceRecord<? extends BasicAdvice>> reversalsTable) {
      // Remove activations
      RequestRecord req = requestTable.getRecord(requestId);
      if (req != null) {
         removeRecordsFromAdvTable(requestId, confirmationsTable);
         removeRecordsFromAdvTable(requestId, reversalsTable);
         requestTable.removeRecord(requestId);
      }
   }

   public void removeRecordsFromAdvTable(
         String requestId,
         AdviceTable<? extends AdviceRecord<? extends BasicAdvice>> adviceTable) {
      List<String> recordsToRemove = new ArrayList<String>();
      Enumeration<? extends AdviceRecord<? extends BasicAdvice>> confRecords = adviceTable.getRecords();
      while (confRecords.hasMoreElements()) {
         AdviceRecord<? extends BasicAdvice> confRecord = confRecords.nextElement();
         if (confRecord.getRequestId().equals(requestId)) {
            recordsToRemove.add(confRecord.getRecordId());
         }
      }
      adviceTable.removeRecords(recordsToRemove);
   }

   public Response handleAddCardDataRequest(List<CardData> cards, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         CardTable cardTable = giftcardDb.getCardTable();
         for (CardData cardData : cards) {
            CardRecord newRecord = cardTable.getRecord(cardData.getCard().getPan());
            if (newRecord == null) {
               newRecord = new CardRecord(cardData.getCard().getPan());
               cardTable.putRecord(newRecord);
            } else {
               newRecord.setActivationId(null);
               newRecord.setLoadIds(new ArrayList<String>());
               newRecord.setRedemptionIds(new ArrayList<String>());
               newRecord.setVoidId(null);
            }
            newRecord.setCard(cardData.getCard());
            newRecord.setAvailableBalance(cardData.getBalance());
            newRecord.setBalance(cardData.getBalance());
            newRecord.setStatus(cardData.getStatus());
            newRecord.setOrigClearPin(cardData.getCard().getClearPin());
            newRecord.setOrigEncPin(cardData.getCard().getEncryptedPin());
            newRecord.setProductId(cardData.getProduct() == null ? null : cardData.getProduct().getId());
         }
         DataResponse dataResponse = (DataResponse) handle(httpHeaders, uriInfo).getEntity();
         return Response.status(201).entity(dataResponse).build();
      } catch (Exception e) {
         log.debug("error processing data request", e);
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
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }

   public Response handleAddProductRequest(List<ProductData> products, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         ProductTable productTable = giftcardDb.getProductTable();
         for (ProductData productData : products) {
            ProductRecord newRecord = productTable.getRecord(productData.getProduct().getId());
            newRecord = new ProductRecord(productData.getProduct().getId());
            productTable.putRecord(newRecord);
            newRecord.setProduct(productData.getProduct());
            newRecord.setStartingBalance(productData.getStartingBalance());
         }
         DataResponse dataResponse = (DataResponse) handle(httpHeaders, uriInfo).getEntity();
         return Response.status(201).entity(dataResponse).build();
      } catch (Exception e) {
         log.debug("error processing data request", e);
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }

   public Response handleDeleteProductRequest(String productId, HttpHeaders httpHeaders, UriInfo uriInfo) {
      try {
         String authString = GiftcardModelUtils.getAuthString(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION));
         String username = GiftcardModelUtils.getUsernameFromAuth(authString);
         String password = GiftcardModelUtils.getPasswordFromAuth(authString);
         // get the DB for this user
         MockGiftcardDb giftcardDb = GiftcardTestServer.getBackend().getDbForUser(username, password);
         Enumeration<CardRecord> cardRecords = giftcardDb.getCardTable().getRecords();
         List<String> cardsToRemove = new ArrayList<String>();
         while (cardRecords.hasMoreElements()) {
            CardRecord cardRecord = cardRecords.nextElement();
            if (productId != null && productId.equals(cardRecord.getProductId())) {
               cardsToRemove.add(cardRecord.getCard().getPan());
            }
         }
         Response response = handle(httpHeaders, uriInfo);
         for (String cardNumber : cardsToRemove) {
            if (response.getStatus() >= 400) {
               // something's gone wrong - just return the response
               return response;
            }
            response = handleDeleteCardRequest(cardNumber, httpHeaders, uriInfo);
         }
         // once all cards for the product have been removed, just remove the product itself
         giftcardDb.getProductTable().removeRecord(productId);
         response = handle(httpHeaders, uriInfo);
         return response;
      } catch (Exception e) {
         log.debug("error processing data request", e);
         Response rsp = Response.serverError().build();
         return rsp;
      }
   }
}
