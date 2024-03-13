/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.type;

public enum In {
  PATH,
  QUERY;

  public static In fromString(String type) {
    return switch (type.trim().toLowerCase()) {
      case "path" -> In.PATH;
      case "query" -> In.QUERY;
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
}
