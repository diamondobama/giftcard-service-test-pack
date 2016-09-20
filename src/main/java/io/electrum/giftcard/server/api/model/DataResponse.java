package io.electrum.giftcard.server.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.vas.Utils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A response to a request to obtain test data from the Test Server.
 */
@ApiModel(description = "A response to a request to obtain test data from the Test Server.")
public class DataResponse {

   private List<CardData> cards = new ArrayList<CardData>();
   private List<ProductData> products = new ArrayList<ProductData>();

   public DataResponse cards(List<CardData> cards) {
      this.cards = cards;
      return this;
   }

   /**
    * The cards configured on the Test Server.
    * 
    * @return acknowledgement
    **/
   @ApiModelProperty(required = true, value = "The cards configured on the Test Server.")
   @JsonProperty("cards")
   @NotNull
   public List<CardData> getCards() {
      return cards;
   }

   public void setCards(List<CardData> cards) {
      this.cards = cards;
   }

   public DataResponse products(List<ProductData> products) {
      this.products = products;
      return this;
   }

   /**
    * The products configured on the Test Server.
    * 
    * @return declaration
    **/
   @ApiModelProperty(required = true, value = "The products configured on the Test Server.")
   @JsonProperty("products")
   @NotNull
   public List<ProductData> getProducts() {
      return products;
   }

   public void setProducts(List<ProductData> products) {
      this.products = products;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      DataResponse cardData = (DataResponse) o;
      return Objects.equals(cards, cardData.cards) && Objects.equals(products, cardData.products);
   }

   @Override
   public int hashCode() {
      return Objects.hash(cards, products);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class DataResponse {\n");

      sb.append("    cards: ").append(Utils.toIndentedString(cards)).append("\n");
      sb.append("    products: ").append(Utils.toIndentedString(products)).append("\n");
      sb.append("}");
      return sb.toString();
   }
}
