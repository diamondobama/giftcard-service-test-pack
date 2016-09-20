package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.VoidConfirmation;

public class VoidConfirmationRecord extends GiftcardRecord {
   protected String requestId;
   protected VoidConfirmation voidConfirmation;
   
   private VoidConfirmationRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public VoidConfirmationRecord(String recordId)
   {
      this.recordId = recordId;
   }

   public String getRequestId() {
      return requestId;
   }

   public void setRequestId(String requestId) {
      this.requestId = requestId;
   }

   public String getConfirmationId() {
      return recordId;
   }

   public VoidConfirmation getVoidConfirmation() {
      return voidConfirmation;
   }

   public void setVoidConfirmation(VoidConfirmation voidConfirmation) {
      this.voidConfirmation = voidConfirmation;
   }
}
