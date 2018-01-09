package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.ActivationReversal;

public class ActivationReversalRecord extends AdviceRecord<ActivationReversal> {

   public ActivationReversalRecord(String recordId) {
      this.recordId = recordId;
   }

   public String getReversalId() {
      return recordId;
   }

   public ActivationReversal getActivationReversal() {
      return getAdvice();
   }

   public void setActivationReversal(ActivationReversal activationReversal) {
      setAdvice(activationReversal);
   }
}
