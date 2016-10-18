package com.activeandroid;

import android.content.Context;

import com.activeandroid.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ActiveAndroid {

  private static HashMap<String,Cache> hashMap=new HashMap<>();

  //////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC METHODS
  //////////////////////////////////////////////////////////////////////////////////////

  public static DbUtils initializeDb(Context context) {
    return initializeDb(new Configuration.Builder(context).create());
  }

  public static DbUtils initializeDb(Configuration configuration) {
    Cache cache = initialize(configuration);
    DbUtils dbUtils =new DbUtils(cache);
    hashMap.put(configuration.getDatabaseName(),cache);
    return dbUtils;
  }

  public static Cache initialize(Configuration configuration) {
    return initialize(configuration, true);
  }

  private static Cache initialize(Configuration configuration, boolean loggingEnabled) {
    // Set logging enabled first
    setLoggingEnabled(loggingEnabled);
    Cache cache = new Cache(configuration);
    return cache;
  }

  private static void setLoggingEnabled(boolean enabled) {
    Log.setEnabled(enabled);
  }

  public static void dispose() {
    if (hashMap==null){
      return;
    }
    Iterator it = hashMap.entrySet().iterator();
    while (it.hasNext()) {
      try{
        Map.Entry pair = (Map.Entry)it.next();
        ((Cache)pair.getValue()).dispose();
        hashMap.remove(pair.getKey());
        it.remove(); // avoids a ConcurrentModificationException
      }catch (Exception e){
        e.printStackTrace();
      }
    }
  }

  public void dispose(Cache cache) {
    cache.dispose();
  }

}