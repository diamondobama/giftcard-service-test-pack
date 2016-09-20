package io.electrum.giftcard.server.api.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.electrum.giftcard.api.model.Product;
import io.electrum.vas.Utils;
import io.electrum.vas.model.LedgerAmount;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Data about a product stored on the Test Server.
 */
@ApiModel(description = "Data about a product stored on the Test Server.")
public class ProductData {

   private Product product = null;
   private LedgerAmount startingBalance = null;

   public ProductData product(Product product) {
      this.product = product;
      return this;
   }

   /**
    * Product data.
    * 
    * @return product
    */
   @ApiModelProperty(required = true, value = "Product data.")
   @JsonProperty("product")
   @NotNull
   public Product getProduct() {
      return product;
   }

   public void setProduct(Product product) {
      this.product = product;
   }

   public ProductData startingBalance(LedgerAmount startingBalance) {
      this.startingBalance = startingBalance;
      return this;
   }

   /**
    * The starting balance of cards activated against the product.
    * 
    * @return balance
    */
   @ApiModelProperty(required = true, value = "The starting balance of cards activated against the product.")
   @JsonProperty("startingBalance")
   @NotNull
   public LedgerAmount getStartingBalance() {
      return startingBalance;
   }

   public void setStartingBalance(LedgerAmount startingBalance) {
      this.startingBalance = startingBalance;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      ProductData cardData = (ProductData) o;
      return Objects.equals(startingBalance, cardData.startingBalance) && Objects.equals(product, cardData.product);
   }

   @Override
   public int hashCode() {
      return Objects.hash(product, startingBalance);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("class ProductData {\n");

      sb.append("    product: ").append(Utils.toIndentedString(product)).append("\n");
      sb.append("    startingBalance: ").append(Utils.toIndentedString(startingBalance)).append("\n");
      sb.append("}");
      return sb.toString();
   }
}
