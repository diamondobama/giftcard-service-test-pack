package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.LoadConfirmation;

public class LoadConfirmationRecord extends GiftcardRecord {
   protected String requestId;
   protected LoadConfirmation loadConfirmation;
   
   private LoadConfirmationRecord()
   {
      //not allowed to create a record without a record ID.
   }
   
   public LoadConfirmationRecord(String recordId)
   {
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

   public LoadConfirmation getLoadConfirmation() {
      return loadConfirmation;
   }

   public void setLoadConfirmation(LoadConfirmation loadConfirmation) {
      this.loadConfirmation = loadConfirmation;
   }
}
