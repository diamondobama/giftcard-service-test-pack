package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.VoidConfirmation;

public class VoidConfirmationRecord extends AdviceRecord<VoidConfirmation> {
   
   public VoidConfirmationRecord(String recordId)
   {
      this.recordId = recordId;
   }

   public String getConfirmationId() {
      return recordId;
   }

   public VoidConfirmation getVoidConfirmation() {
      return getAdvice();
   }

   public void setVoidConfirmation(VoidConfirmation voidConfirmation) {
      setAdvice(voidConfirmation);
   }
}
