package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.ActivationReversal;

public class ActivationReversalRecord extends GiftcardRecord {
   protected String requestId;
   protected ActivationReversal activationReversal;

   private ActivationReversalRecord() {
      // not allowed to create a record without a record ID.
   }

   public ActivationReversalRecord(String recordId) {
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

   public ActivationReversal getActivationReversal() {
      return activationReversal;
   }

   public void setActivationReversal(ActivationReversal activationReversal) {
      this.activationReversal = activationReversal;
   }
}
