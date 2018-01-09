package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.ActivationConfirmation;

public class ActivationConfirmationRecord extends AdviceRecord<ActivationConfirmation> {
   
   public ActivationConfirmationRecord(String recordId)
   {
      this.recordId = recordId;
   }

   public String getConfirmationId() {
      return recordId;
   }

   public ActivationConfirmation getActivationConfirmation() {
      return getAdvice();
   }

   public void setActivationConfirmation(ActivationConfirmation activationConfirmation) {
      setAdvice(activationConfirmation);
   }
}
