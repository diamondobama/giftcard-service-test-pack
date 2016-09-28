package io.electrum.giftcard.server.backend.records;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.electrum.giftcard.api.model.Card;
import io.electrum.vas.model.LedgerAmount;

public class CardRecord extends GiftcardRecord {

   public enum Status {
      NEW, ACTIVATED, ACTIVATED_CONFIRMED, VOIDED, VOIDED_CONFIRMED;
   }

   protected Card card;
   protected String productId;
   protected Status status;
   protected LedgerAmount balance;
   protected LedgerAmount availableBalance;
   protected String activationId;
   protected List<String> loadIds;
   protected List<String> redemptionIds;
   protected String voidId;
   protected String origClearPin;
   protected String origEncPin;

   private CardRecord() {
      // not allowed to create a record without a record ID.
   }

   public CardRecord(String recordId) {
      loadIds = new ArrayList<String>();
      redemptionIds = new ArrayList<String>();
      this.recordId = recordId;
   }
   
   public boolean cardExpiryValid(String dateToCheck)
   {
      return expiryDateCorrect(dateToCheck) && !cardExpired();
   }

   public boolean expiryDateCorrect(String dateToCheck)
   {
      if (card == null) {
         throw new IllegalStateException("No card assocaited with cardRecord!");
      }
      return card.getExpiryDate().equals(dateToCheck);
   }
   
   public boolean cardExpired() {
      if (card == null) {
         throw new IllegalStateException("No card assocaited with cardRecord!");
      }
      Calendar now = Calendar.getInstance();
      int thisYear = now.get(Calendar.YEAR)%100;
      String expiryDate = card.getExpiryDate();
      int expiryYear = Integer.parseInt(expiryDate.substring(0, 2));
      if(thisYear > expiryYear){
         return true;
      }
      else if(thisYear == expiryYear){
         int thisMonth = now.get(Calendar.MONTH)+1;
         int expiryMonth = Integer.parseInt(expiryDate.substring(2));
         if(thisMonth > expiryMonth){
            return true;
         }
      }
      return false;
   }

   public Card getCard() {
      return card;
   }

   public void setCard(Card card) {
      this.card = card;
   }

   public String getProductId() {
      return productId;
   }

   public void setProductId(String productId) {
      this.productId = productId;
   }

   public Status getStatus() {
      return status;
   }

   public void setStatus(Status status) {
      this.status = status;
   }

   public LedgerAmount getBalance() {
      return balance;
   }

   public void setBalance(LedgerAmount balance) {
      this.balance = balance;
   }

   public LedgerAmount getAvailableBalance() {
      return availableBalance;
   }

   public void setAvailableBalance(LedgerAmount availableBalance) {
      this.availableBalance = availableBalance;
   }

   public String getActivationId() {
      return activationId;
   }

   public void setActivationId(String activationId) {
      this.activationId = activationId;
   }

   public List<String> getLoadIds() {
      return loadIds;
   }

   public void setLoadIds(List<String> loadIds) {
      this.loadIds = loadIds;
   }

   public List<String> getRedemptionIds() {
      return redemptionIds;
   }

   public void setRedemptionIds(List<String> redemptionIds) {
      this.redemptionIds = redemptionIds;
   }

   public String getVoidId() {
      return voidId;
   }

   public void setVoidId(String voidId) {
      this.voidId = voidId;
   }

   public void addLoadId(String confirmationId) {
      loadIds.add(confirmationId);
   }

   public void addRedemptionId(String reversalId) {
      redemptionIds.add(reversalId);
   }

   public String getNthLastId(List<String> list, int index) {
      return list.get(list.size() - index);
   }

   public String getLastLoadId() {
      return getNthLastId(loadIds, 1);
   }

   public String getLastRedemptionId() {
      return getNthLastId(redemptionIds, 1);
   }

   public String getOrigClearPin() {
      return origClearPin;
   }

   public void setOrigClearPin(String origClearPin) {
      this.origClearPin = origClearPin;
   }

   public String getOrigEncPin() {
      return origEncPin;
   }

   public void setOrigEncPin(String origEncPin) {
      this.origEncPin = origEncPin;
   }
}
