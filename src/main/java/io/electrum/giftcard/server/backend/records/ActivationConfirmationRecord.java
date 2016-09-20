package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.ActivationConfirmation;

public class ActivationConfirmationRecord extends GiftcardRecord {
   protected String requestId;
   protected ActivationConfirmation activationConfirmation;
   
   private ActivationConfirmationRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public ActivationConfirmationRecord(String recordId)
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

   public ActivationConfirmation getActivationConfirmation() {
      return activationConfirmation;
   }

   public void setActivationConfirmation(ActivationConfirmation activationConfirmation) {
      this.activationConfirmation = activationConfirmation;
   }
}
