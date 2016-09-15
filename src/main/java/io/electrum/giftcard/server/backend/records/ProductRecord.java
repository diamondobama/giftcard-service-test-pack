package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.Product;
import io.electrum.vas.model.LedgerAmount;

public class ProductRecord extends GiftcardRecord {
   protected Product product;
   protected LedgerAmount startingBalance;

   private ProductRecord() {
      // not allowed to create a record without a record ID.
   }

   public ProductRecord(String recordId) {
      this.recordId = recordId;
   }

   public Product getProduct() {
      return product;
   }

   public void setProduct(Product product) {
      this.product = product;
   }

   public LedgerAmount getStartingBalance() {
      return startingBalance;
   }

   public void setStartingBalance(LedgerAmount startingBalance) {
      this.startingBalance = startingBalance;
   }
}
