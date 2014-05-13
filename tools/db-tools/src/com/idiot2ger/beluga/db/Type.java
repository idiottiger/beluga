package com.idiot2ger.beluga.db;

public enum Type {
  INTEGER("INTEGER"), TEXT("TEXT"), BLOB("BLOB"), ;

  String value;

  private Type(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
