/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.utils;

public final class StringUtils {

  private StringUtils() {}

  public static String capitalizeFirstChar(String str) {
    if (str == null || str.isEmpty()) {
      throw new IllegalArgumentException("input string cannot be null or empty");
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }
}
