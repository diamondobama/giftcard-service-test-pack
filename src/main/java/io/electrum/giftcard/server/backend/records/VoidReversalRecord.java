package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.VoidReversal;

public class VoidReversalRecord extends AdviceRecord<VoidReversal> {

   public VoidReversalRecord(String recordId) {
      this.recordId = recordId;
   }

   public String getReversalId() {
      return recordId;
   }

   public VoidReversal getVoidReversal() {
      return getAdvice();
   }

   public void setVoidReversal(VoidReversal voidReversal) {
      setAdvice(voidReversal);
   }
}
