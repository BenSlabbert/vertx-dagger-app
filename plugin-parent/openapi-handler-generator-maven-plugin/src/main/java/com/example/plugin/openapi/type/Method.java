/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.type;

public enum Method {
  GET,
  POST,
  PUT,
  PATCH,
  DELETE;

  public String print() {
    return switch (this) {
      case GET -> "get";
      case POST -> "post";
      case PUT -> "put";
      case PATCH -> "patch";
      case DELETE -> "delete";
    };
  }

  public static Method fromString(String in) {
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
