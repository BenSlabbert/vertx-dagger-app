/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

enum SchemaType {
  ARRAY,
  OBJECT;

  static SchemaType fromString(String type) {
    return switch (type.trim().toLowerCase()) {
      case "array" -> SchemaType.ARRAY;
      case "object" -> SchemaType.OBJECT;
      default -> throw new IllegalArgumentException("Unknown type: " + type);
    };
  }
}
