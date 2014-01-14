package com.idiot2ger.beluga.database;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.database.Cursor;

import com.idiot2ger.beluga.database.ResultColumnInfo.ColumnType;



/**
 * {@link ResultColumnInfo} parser and manager
 * 
 * @author idiot2ger
 * 
 */
class ResultColumnInfoManager {

  private static ResultColumnInfoManager sManager;

  public static ResultColumnInfoManager getInstance() {
    if (sManager == null) {
      sManager = new ResultColumnInfoManager();
    }
    return sManager;
  }

  private HashMap<Class<?>, List<ResultColumnInfoItem>> mColumnInfoCache =
      new HashMap<Class<?>, List<ResultColumnInfoItem>>();

  private ResultColumnInfoManager() {

  }


  /**
   * from class to get the {@link ResultColumnInfo} items
   * 
   * @param cls
   * @return
   */
  private List<ResultColumnInfoItem> findColumnInfoItems(Class<?> cls) {
    if (cls == null) {
      throw new NullPointerException("class must not null");
    }

    // query first
    if (mColumnInfoCache.get(cls) == null) {
      // search all filed
      Field[] fileds = cls.getFields();
      if (fileds == null) {
        throw new IllegalStateException(cls.getName() + " must have field");
      } else {
        final List<ResultColumnInfoItem> columnInfoItems = new ArrayList<ResultColumnInfoItem>();
        for (Field f : fileds) {
          final ResultColumnInfo info = f.getAnnotation(ResultColumnInfo.class);
          if (info != null) {
            columnInfoItems.add(new ResultColumnInfoItem(f, info));
          }
        }

        // add to the map
        if (!columnInfoItems.isEmpty()) {
          mColumnInfoCache.put(cls, columnInfoItems);
        }
      }
    }

    return mColumnInfoCache.get(cls);
  }

  /**
   * 
   * @param transcation
   * @param cls
   * @param cursor
   * @return
   */
  public <T> List<T> extraCursorResultByColumnInfo(final int transaction, final Class<T> cls, final Cursor cursor) {
    final List<ResultColumnInfoItem> columnInfoItems = findColumnInfoItems(cls);
    List<T> result = null;
    if (columnInfoItems != null && cursor != null && cursor.getCount() > 0) {
      // get all index
      final List<Integer> columnIndexList = new ArrayList<Integer>(columnInfoItems.size());
      int[] transactionIds = null;
      boolean isNeedGetColumnIndex;
      for (ResultColumnInfoItem item : columnInfoItems) {
        // first check the transcation id
        transactionIds = item.info.transactionIds();
        isNeedGetColumnIndex = false;

        // check current id in the transaction list or not
        if (transactionIds != null) {
          for (int id : transactionIds) {
            if (id == transaction) {
              isNeedGetColumnIndex = true;
              break;
            }
          }
        } else {
          isNeedGetColumnIndex = true;
        }

        // if don't need get index, also add -1 to the list
        columnIndexList.add(isNeedGetColumnIndex ? cursor.getColumnIndex(item.info.columnName()) : -1);
      }

      // let's query
      result = new ArrayList<T>(cursor.getCount());
      int i = 0;
      final int columnIndexSize = columnIndexList.size();
      ResultColumnInfoItem item = null;
      ResultColumnInfo.ColumnType type = null;
      int columnIndex = -1;
      while (cursor.moveToNext()) {
        try {
          // new instance
          T obj = (T) cls.newInstance();
          for (i = 0; i < columnIndexSize; i++) {
            item = columnInfoItems.get(i);
            type = item.info.columnType();
            columnIndex = columnIndexList.get(i);

            // will check the index, if ==-1, mean this column will be ignored
            if (columnIndex != -1) {
              if (type == ColumnType.TYPE_INTEGER) {
                item.field.setInt(obj, cursor.getInt(columnIndex));
              } else if (type == ColumnType.TYPE_FLOAT) {
                item.field.setFloat(obj, cursor.getFloat(columnIndex));
              } else if (type == ColumnType.TYPE_STRING) {
                item.field.set(obj, cursor.getString(columnIndex));
              } else if (type == ColumnType.TYPE_BLOB) {
                item.field.set(obj, cursor.getBlob(columnIndex));
              } else if (type == ColumnType.TYPE_BOOLEAN) {
                item.field.setBoolean(obj, cursor.getInt(columnIndex) == 1);
              }
            }
          }
          i = 0;

          result.add(obj);
        } catch (InstantiationException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IllegalAccessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  public void release() {
    mColumnInfoCache.clear();
  }

  class ResultColumnInfoItem {
    Field field;
    ResultColumnInfo info;

    public ResultColumnInfoItem(Field f, ResultColumnInfo b) {
      field = f;
      info = b;
    }
  }

}
