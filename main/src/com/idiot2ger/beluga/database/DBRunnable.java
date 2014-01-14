package com.idiot2ger.beluga.database;

import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * @author idiottiger
 * 
 */
public interface DBRunnable {

  /**
   * the database running method
   * 
   * @param database
   */
  public void onDBRunning(SQLiteDatabase database);
}
