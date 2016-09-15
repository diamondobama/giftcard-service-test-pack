package io.electrum.giftcard.server.backend.records;

public abstract class GiftcardRecord {
   protected String recordId;

   public String getRecordId() {
      return recordId;
   }

   public void setRecordId(String recordId) {
      this.recordId = recordId;
   }
}
