package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.RedemptionConfirmation;

public class RedemptionConfirmationRecord extends GiftcardRecord {
   protected String requestId;
   protected RedemptionConfirmation redemptionConfirmation;
   
   private RedemptionConfirmationRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public RedemptionConfirmationRecord(String recordId)
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

   public RedemptionConfirmation getRedemptionConfirmation() {
      return redemptionConfirmation;
   }

   public void setRedemptionConfirmation(RedemptionConfirmation redemptionConfirmation) {
      this.redemptionConfirmation = redemptionConfirmation;
   }
}
