package io.electrum.giftcard.server.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

import io.electrum.giftcard.api.model.Card;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.CardRecord.Status;
import io.electrum.giftcard.server.backend.tables.CardTable;
import io.electrum.vas.model.LedgerAmount;

/**
 *
 */
public class CardLoader {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class);
   private final String cardsFileName = "cards.csv";
   private CardTable cardTableToLoad;

   public CardLoader(CardTable cardTableToLoad) {
      this.cardTableToLoad = cardTableToLoad;
   }

   public CardTable getMapToLoad() {
      return cardTableToLoad;
   }

   public void setMapToLoad(CardTable cardTableToLoad) {
      this.cardTableToLoad = cardTableToLoad;
   }

   public void loadCards() throws IOException {
      log.info(String.format("Loading cards from file: %s", cardsFileName));

      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      CsvReader csvReader = null;

      int numRecords = 0;
      try (InputStream is = classloader.getResourceAsStream(cardsFileName);) {
         csvReader = csvReader == null ? new CsvReader(is, Charset.forName("UTF-8")) : csvReader;
         while (csvReader.readRecord()) {
            Card card = new Card();
            card.setPan(csvReader.get(0));
            card.setExpiryDate(csvReader.get(1));
            String clearPin = csvReader.get(2);
            if (clearPin != null && !clearPin.isEmpty()) {
               card.setClearPin(clearPin);
            }
            String encPin = csvReader.get(3);
            if (encPin != null && !encPin.isEmpty()) {
               card.setEncryptedPin(encPin);
            }
            CardRecord cardRecord = new CardRecord(card.getPan());
            cardRecord.setCard(card);
            cardRecord.setBalance(new LedgerAmount().amount(0l).currency("710"));
            cardRecord.setStatus(Status.NEW);
            cardRecord.setOrigClearPin(clearPin);
            cardRecord.setOrigEncPin(encPin);
            cardTableToLoad.putRecord(cardRecord);
            numRecords++;
         }
      } catch (Exception e) {
         log.error("Error parsing cards file. Possibly incorrect format", e);
         throw e;
      } finally {
         if (csvReader != null) {
            csvReader.close();
         }
      }

      log.info(String.format("%d cards loaded successfully from file.", numRecords));
   }
}
