package com.idiot2ger.beluga.dbtools;

import java.util.ArrayList;
import java.util.List;

public class Table implements IDump {
  String name;
  List<Column> columns = new ArrayList<Column>();

  private Table() {

  }

  public static class TableBuilder {

    private Table mTable;

    public TableBuilder(String name) {
      mTable = new Table();
      mTable.name = name;
    }

    public synchronized TableBuilder addColumn(Column column) {
      if (column != null && !mTable.columns.contains(column)) {
        mTable.columns.add(column);
      }
      return this;
    }

    public synchronized TableBuilder addColumns(List<Column> columns) {
      if (columns != null) {
        for (Column column : columns) {
          addColumn(column);
        }
      }
      return this;
    }

    public Table build() {
      return mTable;
    }

  }

  @Override
  public String getDumpSql() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("CREATE TABLE ");
    buffer.append("\"" + name + "\" (\n");
    int size = columns.size();
    for (int i = 0; i < size; i++) {
      buffer.append(columns.get(i).getDumpSql() + ((i == size - 1) ? "" : ",") + "\n");
    }
    buffer.append(");\n");
    return buffer.toString();
  }

  public String getName() {
    return name;
  }

  public List<Column> getColumns() {
    return columns;
  }


}
