package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.RedemptionReversal;

public class RedemptionReversalRecord extends GiftcardRecord {
   protected String requestId;
   protected RedemptionReversal redemptionReversal;

   private RedemptionReversalRecord() {
      // not allowed to create a record without a record ID.
   }

   public RedemptionReversalRecord(String recordId) {
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

   public RedemptionReversal getRedemptionReversal() {
      return redemptionReversal;
   }

   public void setRedemptionReversal(RedemptionReversal redemptionReversal) {
      this.redemptionReversal = redemptionReversal;
   }
}
