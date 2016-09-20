package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.RedemptionRequest;
import io.electrum.giftcard.api.model.RedemptionResponse;

public class RedemptionRecord extends RequestRecord {
   protected RedemptionRequest redemptionRequest;
   protected RedemptionResponse redemptionResponse;
   
   private RedemptionRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public RedemptionRecord(String recordId)
   {
      super();
      this.recordId = recordId;
   }

   public String getRequestId() {
      return recordId;
   }

   public RedemptionRequest getRedemptionRequest() {
      return redemptionRequest;
   }

   public void setRedemptionRequest(RedemptionRequest redemptionRequest) {
      this.redemptionRequest = redemptionRequest;
   }

   public RedemptionResponse getRedemptionResponse() {
      return redemptionResponse;
   }

   public void setRedemptionResponse(RedemptionResponse redemptionResponse) {
      this.redemptionResponse = redemptionResponse;
   }
}
