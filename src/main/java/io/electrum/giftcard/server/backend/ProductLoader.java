package io.electrum.giftcard.server.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

import io.electrum.giftcard.api.model.Product;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.records.ProductRecord;
import io.electrum.giftcard.server.backend.tables.ProductTable;
import io.electrum.vas.model.LedgerAmount;

/**
 *
 */
public class ProductLoader {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class);
   private final String productsFileName = "products.csv";
   private ProductTable productTableToLoad;

   public ProductLoader(ProductTable productTableToLoad) {
      this.productTableToLoad = productTableToLoad;
   }

   public ProductTable getMapToLoad() {
      return productTableToLoad;
   }

   public void setMapToLoad(ProductTable productTableToLoad) {
      this.productTableToLoad = productTableToLoad;
   }

   public void loadProducts() throws IOException {
      log.info(String.format("Loading products from file: %s", productsFileName));

      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      CsvReader csvReader = null;

      int numRecords = 0;
      try (InputStream is = classloader.getResourceAsStream(productsFileName);) {
         csvReader = csvReader == null ? new CsvReader(is, Charset.forName("UTF-8")) : csvReader;
         while (csvReader.readRecord()) {
            Product product = new Product();
            product.setId(csvReader.get(0));
            product.setBarcode(csvReader.get(1));
            product.setType(csvReader.get(2));
            ProductRecord productRecord = new ProductRecord(product.getId());
            productRecord.setProduct(product);
            productRecord
                  .setStartingBalance(new LedgerAmount().amount(Long.parseLong(csvReader.get(3))).currency("710"));
            productTableToLoad.putRecord(productRecord);
            numRecords++;
         }
      } catch (Exception e) {
         log.error("Error parsing products file. Possibly incorrect format", e);
         throw e;
      } finally {
         if (csvReader != null) {
            csvReader.close();
         }
      }

      log.info(String.format("%d products loaded successfully from file.", numRecords));
   }

}
