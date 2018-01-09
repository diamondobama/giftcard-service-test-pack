package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.RedemptionReversal;

public class RedemptionReversalRecord extends AdviceRecord<RedemptionReversal> {

   public RedemptionReversalRecord(String recordId) {
      this.recordId = recordId;
   }

   public String getReversalId() {
      return recordId;
   }

   public RedemptionReversal getRedemptionReversal() {
      return getAdvice();
   }

   public void setRedemptionReversal(RedemptionReversal redemptionReversal) {
      setAdvice(redemptionReversal);
   }
}
