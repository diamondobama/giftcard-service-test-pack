package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.VoidReversal;

public class VoidReversalRecord extends GiftcardRecord {
   protected String requestId;
   protected VoidReversal voidReversal;

   private VoidReversalRecord() {
      // not allowed to create a record without a record ID.
   }

   public VoidReversalRecord(String recordId) {
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

   public VoidReversal getVoidReversal() {
      return voidReversal;
   }

   public void setVoidReversal(VoidReversal voidReversal) {
      this.voidReversal = voidReversal;
   }
}
