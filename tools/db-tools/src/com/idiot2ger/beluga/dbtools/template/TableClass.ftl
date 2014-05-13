package ${classPackageName};

import com.idiot2ger.beluga.db.Type;

public class ${className} {
  public static final String NAME = "${tableName}";

  public static enum COLUMN {
  
    <#list columns as column>
    ${column.name?upper_case}("${column.name}", Type.${column.type}),
	</#list>
	;
  
    public final String name;
    public final Type type;
    
    private COLUMN(String name, Type type){ 
      this.name = name;
      this.type = type;    
    }
  }
}