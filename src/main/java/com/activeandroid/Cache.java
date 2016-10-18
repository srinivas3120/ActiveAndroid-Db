package com.activeandroid;

import android.content.Context;
import com.activeandroid.serializer.TypeSerializer;
import com.activeandroid.util.Log;
import java.util.Collection;
import net.sqlcipher.database.SQLiteDatabase;

public final class Cache {
  //////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC CONSTANTS
  //////////////////////////////////////////////////////////////////////////////////////

  public final int DEFAULT_CACHE_SIZE = 1024;

  //////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE MEMBERS
  //////////////////////////////////////////////////////////////////////////////////////

  private Context sContext;

  private ModelInfo sModelInfo;
  private DatabaseHelper sDatabaseHelper;

  private boolean sIsInitialized = false;

  //////////////////////////////////////////////////////////////////////////////////////
  // CONSTRUCTORS
  //////////////////////////////////////////////////////////////////////////////////////

  public Cache(Configuration configuration) {
    if (sIsInitialized) {
      Log.v("ActiveAndroid already initialized.");
      return;
    }

    sContext = configuration.getContext();
    sModelInfo = new ModelInfo(configuration);
    sDatabaseHelper = new DatabaseHelper(this, configuration);
    SQLiteDatabase sqLiteDatabase = openDatabase();
    Log.v("dbpath: " + sqLiteDatabase.getPath());
    sIsInitialized = true;

    Log.v("ActiveAndroid initialized successfully.");
  }

  public synchronized void clear() {
    Log.v("Cache cleared.");
  }

  public synchronized void dispose() {
    closeDatabase();

    sModelInfo = null;
    sDatabaseHelper = null;

    sIsInitialized = false;

    Log.v("ActiveAndroid disposed. Call initialize to use library.");
  }

  // Database access

  public boolean isInitialized() {
    return sIsInitialized;
  }

  public synchronized SQLiteDatabase openDatabase() {
    return sDatabaseHelper.getWritableDatabase("@Lifeincontr0l@123");
  }

  public synchronized void closeDatabase() {
    sDatabaseHelper.close();
  }

  // Context access

  public Context getContext() {
    return sContext;
  }

  // Entity cache

  public String getIdentifier(Class<? extends Model> type, Long id) {
    return getTableName(type) + "@" + id;
  }

  public String getIdentifier(Model entity) {
    return getIdentifier(entity.getClass(), entity.getId());
  }

  public synchronized void addEntity(Model entity) {
  }

  public synchronized Model getEntity(Class<? extends Model> type, long id) {
    return null;
  }

  public synchronized void removeEntity(Model entity) {
  }
  // Model cache

  public synchronized Collection<TableInfo> getTableInfos() {
    return sModelInfo.getTableInfos();
  }

  public synchronized TableInfo getTableInfo(Class<? extends Model> type) {
    return sModelInfo.getTableInfo(type);
  }

  public synchronized TypeSerializer getParserForType(Class<?> type) {
    return sModelInfo.getTypeSerializer(type);
  }

  public synchronized String getTableName(Class<? extends Model> type) {
    return sModelInfo.getTableInfo(type).getTableName();
  }
}
