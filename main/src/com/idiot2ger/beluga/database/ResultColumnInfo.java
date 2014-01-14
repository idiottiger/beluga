package com.idiot2ger.beluga.database;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * table column info annotation
 * 
 * @author idiot2ger
 * 
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResultColumnInfo {

  enum ColumnType {
    TYPE_NULL, TYPE_INTEGER, TYPE_FLOAT, TYPE_STRING, TYPE_BLOB, TYPE_BOOLEAN
  }

  /**
   * the table column name
   * 
   * @return
   */
  String columnName() default "";

  /**
   * the table column type, must one of {@link ColumnType}
   * 
   * @return
   */
  ColumnType columnType() default ColumnType.TYPE_NULL;

  /**
   * if current column can work on different transcations, default is no set, will use on all
   * transactions, if set, it will work on your giving transaction ids
   * 
   * @return
   */
  int[] transactionIds();

}
