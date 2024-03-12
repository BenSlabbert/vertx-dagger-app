/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

enum Method {
  GET,
  POST,
  PUT,
  PATCH,
  DELETE;

  static Method fromString(String in) {
    return switch (in.trim().toUpperCase()) {
      case "GET" -> GET;
      case "POST" -> POST;
      case "PUT" -> PUT;
      case "PATCH" -> PATCH;
      case "DELETE" -> DELETE;
      default -> throw new IllegalArgumentException("Unknown type: " + in);
    };
  }
}
