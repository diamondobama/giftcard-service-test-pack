package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.ActivationRequest;
import io.electrum.giftcard.api.model.ActivationResponse;

public class ActivationRecord extends RequestRecord {
   protected ActivationRequest activationRequest;
   protected ActivationResponse activationResponse;
   
   private ActivationRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public ActivationRecord(String recordId)
   {
      super();
      this.recordId = recordId;
   }

   public String getRequestId() {
      return recordId;
   }

   public ActivationRequest getActivationRequest() {
      return activationRequest;
   }

   public void setActivationRequest(ActivationRequest activationRequest) {
      this.activationRequest = activationRequest;
   }

   public ActivationResponse getActivationResponse() {
      return activationResponse;
   }

   public void setActivationResponse(ActivationResponse activationResponse) {
      this.activationResponse = activationResponse;
   }
}
