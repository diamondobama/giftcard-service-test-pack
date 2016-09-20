package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.LookupRequest;
import io.electrum.giftcard.api.model.LookupResponse;

public class LookupRecord extends RequestRecord {
   protected LookupRequest lookupRequest;
   protected LookupResponse lookupResponse;
   
   private LookupRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public LookupRecord(String recordId)
   {
      super();
      this.recordId = recordId;
   }

   public String getRequestId() {
      return recordId;
   }

   public LookupRequest getLookupRequest() {
      return lookupRequest;
   }

   public void setLookupRequest(LookupRequest lookupRequest) {
      this.lookupRequest = lookupRequest;
   }

   public LookupResponse getLookupResponse() {
      return lookupResponse;
   }

   public void setLookupResponse(LookupResponse lookupResponse) {
      this.lookupResponse = lookupResponse;
   }
}
