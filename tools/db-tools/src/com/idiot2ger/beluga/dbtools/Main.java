package com.idiot2ger.beluga.dbtools;

import com.idiot2ger.beluga.db.Type;
import com.idiot2ger.beluga.dbtools.Column.ColumnBuilder;
import com.idiot2ger.beluga.dbtools.Table.TableBuilder;
import com.idiot2ger.beluga.dbtools.model.TableModel;

public class Main {

  public static void main(String[] args) {

    TableBuilder tableBuilder = new TableBuilder("Test");

    ColumnBuilder id_cb =
        new ColumnBuilder("ID", Type.INTEGER).setPrimary(true).setAllowNull(false).setAutoIncrease(true)
            .setDefaultValue(100);

    tableBuilder.addColumn(id_cb.build());

    ColumnBuilder name_cb = new ColumnBuilder("Name", Type.TEXT).setAllowNull(false).setAutoIncrease(true);
    tableBuilder.addColumn(name_cb.build());
    System.out.println(tableBuilder.build().getDumpSql());

    TableModel model = TableModel.convertFromTable(tableBuilder.build());

    model.setClassPackageName("com.test");
    
    TemplateManager.runTableClassTemplate(model, "src");

  }

}
