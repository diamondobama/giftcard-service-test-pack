package io.electrum.giftcard.server.backend.records;

import io.electrum.giftcard.api.model.LoadConfirmation;

public class LoadConfirmationRecord extends AdviceRecord<LoadConfirmation> {
   
   public LoadConfirmationRecord(String recordId)
   {
      this.recordId = recordId;
   }

   public String getConfirmationId() {
      return recordId;
   }

   public LoadConfirmation getLoadConfirmation() {
      return getAdvice();
   }

   public void setLoadConfirmation(LoadConfirmation loadConfirmation) {
      setAdvice(loadConfirmation);
   }
}
