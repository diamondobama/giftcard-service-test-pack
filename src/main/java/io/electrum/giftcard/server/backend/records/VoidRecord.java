package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.VoidRequest;
import io.electrum.giftcard.api.model.VoidResponse;

public class VoidRecord extends RequestRecord {
   protected VoidRequest voidRequest;
   protected VoidResponse voidResponse;
   
   private VoidRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public VoidRecord(String recordId)
   {
      super();
      this.recordId = recordId;
   }

   public String getRequestId() {
      return recordId;
   }

   public VoidRequest getVoidRequest() {
      return voidRequest;
   }

   public void setVoidRequest(VoidRequest voidRequest) {
      this.voidRequest = voidRequest;
   }

   public VoidResponse getVoidResponse() {
      return voidResponse;
   }

   public void setVoidResponse(VoidResponse voidResponse) {
      this.voidResponse = voidResponse;
   }
}
