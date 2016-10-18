package com.activeandroid;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.activeandroid.util.Log;
import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by Mudavath Srinivas on 30/09/16.
 */
public final class DbUtils {
  private Cache cache;
  private String dbName;

  public DbUtils(Cache cache) {
    this.cache = cache;
    setDbName(cache.openDatabase().getPath());
  }

  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  public String getDbName() {
    return dbName;
  }

  public Select select() {
    return new Select(cache);
  }

  public Select select(Cache mCache, String... columns) {
    return new Select(cache, columns);
  }

  public Select select(Cache mCache, Select.Column... columns) {
    return new Select(cache, columns);
  }

  public Delete delete() {
    return new Delete(cache);
  }

  public Update update(Class<? extends Model> table) {
    return new Update(cache, table);
  }

  public Cache getCache() {
    return cache;
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  public void clearCache() {
    clearCache(cache);
  }

  public void clearCache(Cache cache) {
    cache.clear();
  }

  public void setLoggingEnabled(boolean enabled) {
    Log.setEnabled(enabled);
  }

  public SQLiteDatabase getDatabase() {
    return getDatabase(cache);
  }

  public SQLiteDatabase getDatabase(Cache cache) {
    return cache.openDatabase();
  }

  public void beginTransaction() {
    beginTransaction(cache);
  }

  public void beginTransaction(Cache cache) {
    cache.openDatabase().beginTransaction();
  }

  public void endTransaction() {
    endTransaction(cache);
  }

  public void endTransaction(Cache cache) {
    cache.openDatabase().endTransaction();
  }

  public void setTransactionSuccessful() {
    setTransactionSuccessful(cache);
  }

  public void setTransactionSuccessful(Cache cache) {
    cache.openDatabase().setTransactionSuccessful();
  }

  public boolean inTransaction() {
    return inTransaction(cache);
  }

  public boolean inTransaction(Cache cache) {
    return cache.openDatabase().inTransaction();
  }

  public void execSQL(String sql) {
    execSQL(cache, sql);
  }

  public void execSQL(Cache cache, String sql) {
    cache.openDatabase().execSQL(sql);
  }

  public void execSQL(String sql, Object[] bindArgs) {
    execSQL(cache, sql, bindArgs);
  }

  public void execSQL(Cache cache, String sql, Object[] bindArgs) {
    cache.openDatabase().execSQL(sql, bindArgs);
  }

  public void attachDb(String path) {
    try {
      detachDb();
      cache.openDatabase()
          .execSQL("ATTACH DATABASE '" + path + "' AS db2 KEY '@Lifeincontr0l@123'");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void attachDb(String path, String password) {
    try {
      detachDb();
      cache.openDatabase().execSQL("ATTACH DATABASE '" + path + "' AS db2 KEY '" + password + "'");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void detachDb() {
    try {
      cache.openDatabase().execSQL("DETACH DATABASE db2");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
