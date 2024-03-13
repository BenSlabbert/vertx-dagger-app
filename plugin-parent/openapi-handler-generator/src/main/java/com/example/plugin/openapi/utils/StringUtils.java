/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi.utils;

import com.example.plugin.openapi.type.Method;

public final class StringUtils {

  private StringUtils() {}

  public static String capitalizeFirstChar(String str) {
    if (str == null || str.isEmpty()) {
      throw new IllegalArgumentException("input string cannot be null or empty");
    }
    return str.substring(0, 1).toUpperCase() + str.substring(1);
  }

  /** The path will look like /users/{id}, so for the above path we will generate: getUsersById */
  public static String getMethodNameFromPath(Method method, String path) {
    String classNameFromPath = getClassNameFromPath(method, path);
    return classNameFromPath.substring(0, 1).toLowerCase() + classNameFromPath.substring(1);
  }

  /**
   * The path will look like /users/{id}, so for the above path we will generate:
   * GetUsersByIdParameterParser
   */
  public static String getParameterParserClassName(Method method, String path) {
    return getClassNameFromPath(method, path) + "ParameterParser";
  }

  private static String getClassNameFromPath(Method method, String path) {
    path = removeIllegalCharsAndCapitalizeNextChar(path);

    String[] parts = path.split("/");
    StringBuilder sb = new StringBuilder();
    for (String part : parts) {
      if (part.isEmpty()) {
        continue;
      }
      if (part.startsWith("{")) {
        sb.append("By");
        part = part.substring(1, part.length() - 1);
      }
      sb.append(capitalizeFirstChar(part));
    }
    return capitalizeFirstChar(method.print()) + sb;
  }

  /** makes the name a safe java variable name */
  public static String variableName(String name) {
    name = removeIllegalCharsAndCapitalizeNextChar(name);
    return name.replaceAll("[^a-zA-Z0-9]", "");
  }

  private static String removeIllegalCharsAndCapitalizeNextChar(String in) {
    char[] removeChars = {'-', '_'};
    for (char removeChar : removeChars) {
      int index = in.indexOf(removeChar);
      while (index != -1) {
        in = in.substring(0, index) + capitalizeFirstChar(in.substring(index + 1));
        index = in.indexOf(removeChar);
      }
    }
    return in;
  }
}
