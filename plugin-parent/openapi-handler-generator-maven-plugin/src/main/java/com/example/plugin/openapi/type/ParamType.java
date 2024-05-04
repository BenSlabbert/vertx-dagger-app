/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.type;

public enum ParamType {
  INTEGER,
  STRING,
  BOOLEAN;

  public String print() {
    return switch (this) {
      case INTEGER -> "int";
      case STRING -> "String";
      case BOOLEAN -> "boolean";
    };
  }

  public String printParserPrefix() {
    return switch (this) {
      case INTEGER -> "Integer";
      case STRING -> "String";
      case BOOLEAN -> "Boolean";
    };
  }

  public static ParamType fromString(String type) {
    return switch (type.trim().toLowerCase()) {
      case "integer" -> ParamType.INTEGER;
      case "string" -> ParamType.STRING;
      case "boolean" -> ParamType.BOOLEAN;
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
}
