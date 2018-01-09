package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.RedemptionConfirmation;

public class RedemptionConfirmationRecord extends AdviceRecord<RedemptionConfirmation> {
   
   public RedemptionConfirmationRecord(String recordId)
   {
      this.recordId = recordId;
   }

   public String getConfirmationId() {
      return recordId;
   }

   public RedemptionConfirmation getRedemptionConfirmation() {
      return getAdvice();
   }

   public void setRedemptionConfirmation(RedemptionConfirmation redemptionConfirmation) {
      setAdvice(redemptionConfirmation);
   }
}
