package io.electrum.giftcard.handler;

public class GiftcardMessageHandlerFactory {
   public static ConfirmActivationHandler getConfirmActivationHandler() {
      return new ConfirmActivationHandler();
   }

   public static ActivationHandler getActivationHandler() {
      return new ActivationHandler();
   }

   public static ReverseActivationHandler getReverseActivationHandler() {
      return new ReverseActivationHandler();
   }

   public static ConfirmLoadHandler getConfirmLoadHandler() {
      return new ConfirmLoadHandler();
   }

   public static LoadHandler getLoadHandler() {
      return new LoadHandler();
   }

   public static LookupGiftcardHandler getLookupGiftcardHandler() {
      return new LookupGiftcardHandler();
   }

   public static ReverseLoadHandler getReverseLoadHandler() {
      return new ReverseLoadHandler();
   }

   public static ConfirmRedemptionHandler getConfirmRedemptionHandler() {
      return new ConfirmRedemptionHandler();
   }

   public static RedemptionHandler getRedemptionHandler() {
      return new RedemptionHandler();
   }

   public static ReverseRedemptionHandler getReverseRedemptionHandler() {
      return new ReverseRedemptionHandler();
   }

   public static ConfirmVoidHandler getConfirmVoidHandler() {
      return new ConfirmVoidHandler();
   }

   public static VoidHandler getVoidHandler() {
      return new VoidHandler();
   }

   public static ReverseVoidHandler getReverseVoidHandler() {
      return new ReverseVoidHandler();
   }

   public static ResetHandler getResetHandler() {
      return new ResetHandler();
   }

   public static DataHandler getDataHandler() {
      return new DataHandler();
   }
}
