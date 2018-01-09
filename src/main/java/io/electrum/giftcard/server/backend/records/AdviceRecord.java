package io.electrum.giftcard.server.backend.records;

import io.electrum.vas.model.BasicAdvice;

public abstract class AdviceRecord<T extends BasicAdvice> extends GiftcardRecord {
   protected String requestId;
   protected T advice;

   protected AdviceRecord() {
      // not allowed to create a record without a record ID.
   }

   public AdviceRecord(String recordId) {
      this.recordId = recordId;
   }

   public String getRequestId() {
      return requestId;
   }

   public void setRequestId(String requestId) {
      this.requestId = requestId;
   }

   public String getConfirmationId() {
      return recordId;
   }

   public T getAdvice() {
      return advice;
   }

   public void setAdvice(T advice) {
      this.advice = advice;
   }
}
