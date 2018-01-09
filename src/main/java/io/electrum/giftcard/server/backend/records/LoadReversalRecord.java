package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.LoadReversal;

public class LoadReversalRecord extends AdviceRecord<LoadReversal> {

   public LoadReversalRecord(String recordId) {
      this.recordId = recordId;
   }

   public String getReversalId() {
      return recordId;
   }

   public LoadReversal getLoadReversal() {
      return getAdvice();
   }

   public void setLoadReversal(LoadReversal loadReversal) {
      setAdvice(loadReversal);
   }
}
