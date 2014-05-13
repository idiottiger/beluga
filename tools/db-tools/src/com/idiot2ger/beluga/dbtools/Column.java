package com.idiot2ger.beluga.dbtools;

import com.idiot2ger.beluga.db.Type;

public class Column implements IDump {

  String name;
  Type type;
  boolean primary;
  boolean autoIncrease;
  boolean allowNull = true;
  Object defaultValue;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  private Column() {

  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Column) {
      return ((Column) obj).name.equals(name);
    }
    return false;
  }

  @Override
  public String getDumpSql() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\"" + name + "\" ");
    buffer.append(type.getValue() + " ");
    if (primary) {
      buffer.append("PRIMARY KEY ");
    }
    if (autoIncrease) {
      buffer.append("AUTOINCREMENT ");
    }
    if (!allowNull) {
      buffer.append("NOT NULL ");
    }
    if (defaultValue != null) {
      buffer.append("DEFAULT ");
      if (type == Type.INTEGER) {
        buffer.append(defaultValue);
      } else {
        buffer.append("\"" + defaultValue + "\"");
      }
    }
    return buffer.toString();
  }



  public static class ColumnBuilder {
    Column mColumn;

    public ColumnBuilder(String name, Type type) {
      mColumn = new Column();
      mColumn.name = name;
      mColumn.type = type;
    }

    public ColumnBuilder setPrimary(boolean primary) {
      mColumn.primary = primary;
      if (primary) {
        setAllowNull(false);
      }
      return this;
    }

    public ColumnBuilder setAutoIncrease(boolean autoIncrease) {
      if (mColumn.type == Type.INTEGER) {
        mColumn.autoIncrease = autoIncrease;
      } else {
        System.err.println("Column[name:" + mColumn.name + ", type:" + mColumn.type.getValue()
            + "] cann't support auto increase, ONLY work on INTEGER type");
      }
      return this;
    }

    public ColumnBuilder setAllowNull(boolean allowNull) {
      mColumn.allowNull = allowNull;
      return this;
    }

    public ColumnBuilder setDefaultValue(Object value) {
      mColumn.defaultValue = value;
      return this;
    }

    public Column build() {
      return mColumn;
    }

  }


}
