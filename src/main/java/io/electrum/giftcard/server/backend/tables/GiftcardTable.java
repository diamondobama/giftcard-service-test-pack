package io.electrum.giftcard.server.backend.tables;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.electrum.giftcard.server.backend.records.GiftcardRecord;

public abstract class GiftcardTable<T extends GiftcardRecord> {
   private ConcurrentHashMap<String, T> records;

   public GiftcardTable() {
      records = new ConcurrentHashMap<String, T>();
   }

   public T getRecord(String recordId) {
      if (records == null) {
         throw new IllegalStateException("Table not initialised yet!");
      }
      if (recordId == null) {
         return null;
      }
      return records.get(recordId);
   }

   public void putRecord(T record) {
      if (record.getRecordId() == null) {
         throw new IllegalStateException("Cannot store record without a recordId (used as primary key)");
      }
      records.put(record.getRecordId(), record);
   }

   public Enumeration<T> getRecords() {
      return records.elements();
   }

   public T removeRecord(String recordId) {
      return records.remove(recordId);
   }

   public void removeRecords(List<String> recordIds) {
      for (String recordId : recordIds) {
         records.remove(recordId);
      }
   }
}
