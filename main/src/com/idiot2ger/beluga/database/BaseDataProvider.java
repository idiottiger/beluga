package com.idiot2ger.beluga.database;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;


/**
 * a simple and typical content provider impelements.</br> you need bind a {@link SQLiteOpenHelper}
 * to this provider use method {@link #setSQLiteOpenHelper(SQLiteOpenHelper)}.
 * 
 * @author idiottiger
 * @version 1.0
 */
public class BaseDataProvider extends ContentProvider {

  public static final String LOG_TAG = "BaseDataProvider";

  protected SQLiteDatabase database;

  protected SQLiteOpenHelper helper;

  /**
   * set the sqlite open helper, this method should invoke before query database happen, suggestion
   * use in {@link #onCreate()} method.
   * 
   * @param helper
   */
  public void setSQLiteOpenHelper(SQLiteOpenHelper helper) {
    this.helper = helper;
  }

  private void initDataBase() {
    if (helper != null && (database == null || !database.isOpen())) {
      database = helper.getWritableDatabase();
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    if (uri == null) {
      Log.e(LOG_TAG, "request uri is null.");
      return -1;
    }

    String tablename = getTableName(uri);
    if (tablename == null || tablename.trim().length() == 0) {
      Log.e(LOG_TAG, "request uri:" + uri + " error.");
      return -1;
    }

    initDataBase();
    if (database == null) {
      Log.e(LOG_TAG, "database is not exitsed.");
      return -1;
    }

    int rows = database.delete(tablename, selection, selectionArgs);
    closeDabaBase();
    return rows;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    if (uri == null) {
      Log.e(LOG_TAG, "request uri is null.");
      return null;
    }

    if (values == null || values.size() == 0) {
      Log.e(LOG_TAG, "the record you want to insert is null or empty.");
      return null;
    }

    String tablename = getTableName(uri);
    if (tablename == null || tablename.trim().length() == 0) {
      Log.e(LOG_TAG, "request uri:" + uri + " error.");
      return null;
    }

    initDataBase();
    if (database == null) {
      Log.e(LOG_TAG, "database is not exitsed.");
      return null;
    }

    // WARNING:using the trancsaction to fix the data insert error to
    // rollback
    // The insert return value > 0 mean ok , < 0 mean error ,when the error
    // happen,the data need to rollback
    database.beginTransaction();
    long rowid = database.insert(tablename, null, values);
    if (rowid > 0) {
      database.setTransactionSuccessful();
      Log.d(LOG_TAG, "record insert successful.");
    } else {
      Log.e(LOG_TAG, "record insert error.");
      database.endTransaction();
      return null;
    }
    database.endTransaction();
    closeDabaBase();
    return ContentUris.withAppendedId(uri, rowid);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    if (uri == null) {
      Log.e(LOG_TAG, "request uri is null.");
      return null;
    }

    String tablename = getTableName(uri);
    if (tablename == null || tablename.trim().length() == 0) {
      Log.e(LOG_TAG, "request uri:" + uri + " error.");
      return null;
    }

    initDataBase();
    if (database == null) {
      Log.e(LOG_TAG, "database is not exitsed.");
      return null;
    }

    Cursor cursor = database.query(tablename, projection, selection, selectionArgs, null, null, sortOrder);
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    if (uri == null) {
      Log.e(LOG_TAG, "request uri is null.");
      return -1;
    }

    if (values == null || values.size() == 0) {
      Log.e(LOG_TAG, "the record you want to insert is null or empty.");
      return -1;
    }

    String tablename = getTableName(uri);
    if (tablename == null || tablename.trim().length() == 0) {
      Log.e(LOG_TAG, "request uri:" + uri + " error.");
      return -1;
    }

    if (database == null) {
      initDataBase();
      if (database == null) {
        Log.e(LOG_TAG, "database is not exitsed.");
        return -1;
      }
    }

    int rows = database.update(tablename, values, selection, selectionArgs);
    closeDabaBase();
    return rows;
  }

  @Override
  public int bulkInsert(Uri uri, ContentValues[] values) {
    if (uri == null) {
      Log.e(LOG_TAG, "request uri is null.");
      return -1;
    }

    if (values == null || values.length == 0) {
      Log.e(LOG_TAG, "the records you want to insert is null or empty.");
      return -1;
    }

    int numValues = values.length;
    for (int i = 0; i < numValues; i++) {
      insert(uri, values[i]);
    }
    return numValues;
  }

  /*
   * return current uri's table name
   */
  private String getTableName(Uri uri) {
    if (uri == null) {
      Log.e(LOG_TAG, "request uri is null.");
      return null;
    }
    return uri.getLastPathSegment();
  }

  @Override
  public boolean onCreate() {
    initDataBase();
    return true;
  }

  public void closeDabaBase() {
    // RPC invoke, so dont close
  }

}
