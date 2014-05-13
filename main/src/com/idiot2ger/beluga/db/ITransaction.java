package com.idiot2ger.beluga.db;

/**
 * 
 * @author idiot2ger
 * 
 */
public interface ITransaction<T> {

  public long insert(ISqliteDb db);

  public long update(ISqliteDb db);

  public long delete(ISqliteDb db);

}
