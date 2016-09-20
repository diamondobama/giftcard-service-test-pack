package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.LoadRequest;
import io.electrum.giftcard.api.model.LoadResponse;

public class LoadRecord extends RequestRecord {
   protected LoadRequest loadRequest;
   protected LoadResponse loadResponse;
   
   private LoadRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public LoadRecord(String recordId)
   {
      super();
      this.recordId = recordId;
   }

   public String getRequestId() {
      return recordId;
   }

   public LoadRequest getLoadRequest() {
      return loadRequest;
   }

   public void setLoadRequest(LoadRequest loadRequest) {
      this.loadRequest = loadRequest;
   }

   public LoadResponse getLoadResponse() {
      return loadResponse;
   }

   public void setLoadResponse(LoadResponse loadResponse) {
      this.loadResponse = loadResponse;
   }
}
