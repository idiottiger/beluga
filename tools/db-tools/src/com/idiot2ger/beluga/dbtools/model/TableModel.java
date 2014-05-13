package com.idiot2ger.beluga.dbtools.model;

import java.util.ArrayList;

import com.idiot2ger.beluga.dbtools.Column;
import com.idiot2ger.beluga.dbtools.Table;

public class TableModel extends BaseModel {

  private String tableName;
  private ArrayList<ColumnModel> columns = new ArrayList<ColumnModel>();

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public ArrayList<ColumnModel> getColumns() {
    return columns;
  }

  public void addColumnModel(ColumnModel columnModel) {
    columns.add(columnModel);
  }


  public static TableModel convertFromTable(Table table) {
    TableModel model = new TableModel();
    model.setTableName(table.getName());
    model.setClassName("TABLE_" + table.getName().toUpperCase());

    for (Column column : table.getColumns()) {
      ColumnModel columnModel = new ColumnModel();
      columnModel.setName(column.getName());
      columnModel.setType(column.getType().getValue());

      model.addColumnModel(columnModel);
    }

    return model;
  }

}
