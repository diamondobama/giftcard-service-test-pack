package io.electrum.giftcard.server.backend.db;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.electrum.giftcard.api.model.Card;
import io.electrum.giftcard.api.model.Product;
import io.electrum.giftcard.server.api.GiftcardTestServer;
import io.electrum.giftcard.server.backend.CardLoader;
import io.electrum.giftcard.server.backend.ProductLoader;
import io.electrum.giftcard.server.backend.records.CardRecord;
import io.electrum.giftcard.server.backend.records.ProductRecord;
import io.electrum.giftcard.server.backend.tables.ActivationConfirmationsTable;
import io.electrum.giftcard.server.backend.tables.ActivationReversalsTable;
import io.electrum.giftcard.server.backend.tables.ActivationsTable;
import io.electrum.giftcard.server.backend.tables.CardTable;
import io.electrum.giftcard.server.backend.tables.LoadConfirmationsTable;
import io.electrum.giftcard.server.backend.tables.LoadReversalsTable;
import io.electrum.giftcard.server.backend.tables.LoadsTable;
import io.electrum.giftcard.server.backend.tables.LookupsTable;
import io.electrum.giftcard.server.backend.tables.ProductTable;
import io.electrum.giftcard.server.backend.tables.RedemptionConfirmationsTable;
import io.electrum.giftcard.server.backend.tables.RedemptionReversalsTable;
import io.electrum.giftcard.server.backend.tables.RedemptionsTable;
import io.electrum.giftcard.server.backend.tables.VoidConfirmationsTable;
import io.electrum.giftcard.server.backend.tables.VoidReversalsTable;
import io.electrum.giftcard.server.backend.tables.VoidsTable;

public class MockGiftcardDb {
   private static final Logger log = LoggerFactory.getLogger(GiftcardTestServer.class);

   private ProductTable productTable;
   private CardTable cardTable;
   private ActivationsTable activationsTable;
   private ActivationReversalsTable activationReversalsTable;
   private ActivationConfirmationsTable activationConfirmationsTable;
   private LoadsTable loadsTable;
   private LoadReversalsTable loadReversalsTable;
   private LoadConfirmationsTable loadConfirmationsTable;
   private LookupsTable lookupsTable;
   private RedemptionsTable redemptionsTable;
   private RedemptionReversalsTable redemptionReversalsTable;
   private RedemptionConfirmationsTable redemptionConfirmationsTable;
   private VoidsTable voidsTable;
   private VoidReversalsTable voidReversalsTable;
   private VoidConfirmationsTable voidConfirmationsTable;

   public MockGiftcardDb() {
      reset();
   }

   public void reset() {
      productTable = new ProductTable();
      ProductLoader productLoader = new ProductLoader(productTable);
      try {
         productLoader.loadProducts();
      } catch (IOException ioe) {
         log.debug("Exception while loading products - not loading any. ", ioe);
         productTable = new ProductTable();
      }
      cardTable = new CardTable();
      CardLoader cardLoader = new CardLoader(cardTable);
      try {
         cardLoader.loadCards();
      } catch (IOException ioe) {
         log.debug("Exception while loading cards - not loading any. ", ioe);
         cardTable = new CardTable();
      }
      activationsTable = new ActivationsTable();
      activationReversalsTable = new ActivationReversalsTable();
      activationConfirmationsTable = new ActivationConfirmationsTable();
      loadsTable = new LoadsTable();
      loadReversalsTable = new LoadReversalsTable();
      loadConfirmationsTable = new LoadConfirmationsTable();
      lookupsTable = new LookupsTable();
      redemptionsTable = new RedemptionsTable();
      redemptionReversalsTable = new RedemptionReversalsTable();
      redemptionConfirmationsTable = new RedemptionConfirmationsTable();
      voidsTable = new VoidsTable();
      voidReversalsTable = new VoidReversalsTable();
      voidConfirmationsTable = new VoidConfirmationsTable();
   }

   public CardRecord getCardRecord(Card card) {
      if (card == null) {
         return null;
      }
      return getCardTable().getRecord(card.getPan());
   }

   public ProductRecord getProductRecord(Product product) {
      if (product == null) {
         return null;
      }
      return getProductTable().getRecord(product.getId());
   }

   public boolean doesUuidExist(String uuid) {
      return activationsTable.getRecord(uuid) != null || activationReversalsTable.getRecord(uuid) != null
            || activationConfirmationsTable.getRecord(uuid) != null || loadsTable.getRecord(uuid) != null
            || loadReversalsTable.getRecord(uuid) != null || loadConfirmationsTable.getRecord(uuid) != null
            || lookupsTable.getRecord(uuid) != null || redemptionsTable.getRecord(uuid) != null
            || redemptionReversalsTable.getRecord(uuid) != null || redemptionConfirmationsTable.getRecord(uuid) != null
            || voidsTable.getRecord(uuid) != null || voidReversalsTable.getRecord(uuid) != null
            || voidConfirmationsTable.getRecord(uuid) != null;
   }

   public ProductTable getProductTable() {
      return productTable;
   }

   public void setProductTable(ProductTable productTable) {
      this.productTable = productTable;
   }

   public CardTable getCardTable() {
      return cardTable;
   }

   public void setCardTable(CardTable cardTable) {
      this.cardTable = cardTable;
   }

   public ActivationsTable getActivationsTable() {
      return activationsTable;
   }

   public void setActivationsTable(ActivationsTable activationsTable) {
      this.activationsTable = activationsTable;
   }

   public ActivationReversalsTable getActivationReversalsTable() {
      return activationReversalsTable;
   }

   public void setActivationReversalsTable(ActivationReversalsTable activationReversalsTable) {
      this.activationReversalsTable = activationReversalsTable;
   }

   public ActivationConfirmationsTable getActivationConfirmationsTable() {
      return activationConfirmationsTable;
   }

   public void setActivationConfirmationsTable(ActivationConfirmationsTable activationConfirmationsTable) {
      this.activationConfirmationsTable = activationConfirmationsTable;
   }

   public LoadsTable getLoadsTable() {
      return loadsTable;
   }

   public void setLoadsTable(LoadsTable loadsTable) {
      this.loadsTable = loadsTable;
   }

   public LoadReversalsTable getLoadReversalsTable() {
      return loadReversalsTable;
   }

   public void setLoadReversalsTable(LoadReversalsTable loadReversalsTable) {
      this.loadReversalsTable = loadReversalsTable;
   }

   public LoadConfirmationsTable getLoadConfirmationsTable() {
      return loadConfirmationsTable;
   }

   public void setLoadConfirmationsTable(LoadConfirmationsTable loadConfirmationsTable) {
      this.loadConfirmationsTable = loadConfirmationsTable;
   }

   public LookupsTable getLookupsTable() {
      return lookupsTable;
   }

   public void setLookupsTable(LookupsTable lookupsTable) {
      this.lookupsTable = lookupsTable;
   }

   public RedemptionsTable getRedemptionsTable() {
      return redemptionsTable;
   }

   public void setRedemptionsTable(RedemptionsTable redemptionsTable) {
      this.redemptionsTable = redemptionsTable;
   }

   public RedemptionReversalsTable getRedemptionReversalsTable() {
      return redemptionReversalsTable;
   }

   public void setRedemptionReversalsTable(RedemptionReversalsTable redemptionReversalsTable) {
      this.redemptionReversalsTable = redemptionReversalsTable;
   }

   public RedemptionConfirmationsTable getRedemptionConfirmationsTable() {
      return redemptionConfirmationsTable;
   }

   public void setRedemptionConfirmationsTable(RedemptionConfirmationsTable redemptionConfirmationsTable) {
      this.redemptionConfirmationsTable = redemptionConfirmationsTable;
   }

   public VoidsTable getVoidsTable() {
      return voidsTable;
   }

   public void setVoidsTable(VoidsTable voidsTable) {
      this.voidsTable = voidsTable;
   }

   public VoidReversalsTable getVoidReversalsTable() {
      return voidReversalsTable;
   }

   public void setVoidReversalsTable(VoidReversalsTable voidReversalsTable) {
      this.voidReversalsTable = voidReversalsTable;
   }

   public VoidConfirmationsTable getVoidConfirmationsTable() {
      return voidConfirmationsTable;
   }

   public void setVoidConfirmationsTable(VoidConfirmationsTable voidConfirmationsTable) {
      this.voidConfirmationsTable = voidConfirmationsTable;
   }
}
