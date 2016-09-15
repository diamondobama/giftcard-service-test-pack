package io.electrum.giftcard.server.backend;

import java.util.concurrent.ConcurrentHashMap;

import io.electrum.giftcard.server.backend.db.MockGiftcardDb;

public class GiftcardBackend {
   private static GiftcardBackend instance;
   private static ConcurrentHashMap<String, MockGiftcardDb> dbs;
   
   private GiftcardBackend()
   {
      dbs = new ConcurrentHashMap<String, MockGiftcardDb>();
   }
   
   public static GiftcardBackend getInstance()
   {
      if(instance == null)
      {
         instance = new GiftcardBackend();
      }
      return instance;
   }
   
   public MockGiftcardDb getDbForUser(String username, String password)
   {
      String dbKey = username+"|"+password;
      MockGiftcardDb db = dbs.get(dbKey);
      if(db == null)
      {
         db = new MockGiftcardDb();
         dbs.put(dbKey, db);
      }
      return db;
   }
   
   public boolean doesDbForUserExist(String username, String password)
   {
      String dbKey = username+"|"+password;
      MockGiftcardDb db = dbs.get(dbKey);
      if(db == null)
      {
         return false;
      }
      return true;
   }
}
