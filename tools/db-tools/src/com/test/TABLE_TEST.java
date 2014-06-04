package com.test;

import com.idiot2ger.beluga.db.Type;

public class TABLE_TEST {
  public static final String NAME = "Test";

  public static enum COLUMN {
  
    ID("ID", Type.INTEGER),
    NAME("Name", Type.TEXT),
	;
  
    public final String name;
    public final Type type;
    
    private COLUMN(String name, Type type){ 
      this.name = name;
      this.type = type;    
    }
  }
}