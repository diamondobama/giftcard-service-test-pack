package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.LoadReversal;

public class LoadReversalRecord extends GiftcardRecord {
   protected String requestId;
   protected LoadReversal loadReversal;

   private LoadReversalRecord() {
      // not allowed to create a record without a record ID.
   }

   public LoadReversalRecord(String recordId) {
      this.recordId = recordId;
   }

   public String getRequestId() {
      return requestId;
   }

   public void setRequestId(String requestId) {
      this.requestId = requestId;
   }

   public String getReversalId() {
      return recordId;
   }

   public LoadReversal getLoadReversal() {
      return loadReversal;
   }

   public void setLoadReversal(LoadReversal loadReversal) {
      this.loadReversal = loadReversal;
   }
}
