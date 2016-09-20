package io.electrum.giftcard.server.api.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.giftcard.api.model.Card;
import io.electrum.giftcard.api.model.Product;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.vas.Utils;
import io.electrum.vas.model.LedgerAmount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Data about a card stored on the Test Server.
 */
@ApiModel(description = "Data about a card stored on the Test Server.")
public class CardData {

   private Card card = null;
   private LedgerAmount balance = null;
   private CardRecord.Status status = null;
   private Product product = null;

   public CardData card(Card card) {
      this.card = card;
      return this;
   }

   /**
    * Card data.
    * 
    * @return card
    */
   @ApiModelProperty(required = true, value = "Card data.")
   @JsonProperty("card")
   @NotNull
   public Card getCard() {
      return card;
   }

   public void setCard(Card card) {
      this.card = card;
   }

   public CardData balance(LedgerAmount balance) {
      this.balance = balance;
      return this;
   }

   /**
    * The balance of the card.
    * 
    * @return balance
    */
   @ApiModelProperty(required = true, value = "The balance of the card.")
   @JsonProperty("balance")
   @NotNull
   public LedgerAmount getBalance() {
      return balance;
   }

   public void setBalance(LedgerAmount balance) {
      this.balance = balance;
   }

   public CardData status(CardRecord.Status status) {
      this.status = status;
      return this;
   }

   /**
    * The status of the card.
    * 
    * @return status
    */
   @ApiModelProperty(required = true, value = "The status of the card.")
   @JsonProperty("status")
   @NotNull
   public CardRecord.Status getStatus() {
      return status;
   }

   public void setStatus(CardRecord.Status status) {
      this.status = status;
   }

   public CardData product(Product product) {
      this.product = product;
      return this;
   }

   /**
    * Product data associated with the card. Only relevant if the card is activated.
    * 
    * @return product
    */
   @ApiModelProperty(value = "Product data associated with the card. Only relevant if the card is activated.")
   @JsonProperty("product")
   public Product getProduct() {
      return product;
   }

   public void setProduct(Product product) {
      this.product = product;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      CardData cardData = (CardData) o;
      return Objects.equals(card, cardData.card) && Objects.equals(balance, cardData.balance)
            && Objects.equals(status, cardData.status) && Objects.equals(product, cardData.product);
   }

   @Override
   public int hashCode() {
      return Objects.hash(card, balance, status, product);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class CardData {\n");

      sb.append("    card: ").append(Utils.toIndentedString(card)).append("\n");
      sb.append("    balance: ").append(Utils.toIndentedString(balance)).append("\n");
      sb.append("    status: ").append(Utils.toIndentedString(status)).append("\n");
      sb.append("    product: ").append(Utils.toIndentedString(product)).append("\n");
      sb.append("}");
      return sb.toString();
   }
}
