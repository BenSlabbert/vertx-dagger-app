/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url.generator;

final class PathSanitizer {

  private PathSanitizer() {}

  static String sanitize(String path) {
    // replace /{int:name}/ with just /:name/
    // replace /{long:name}/ with just /:name/
    // replace /{string:name}/ with just /:name/
    path = path.replaceAll("/\\{int:([^}]+)}/", "/:$1/");
    path = path.replaceAll("/\\{long:([^}]+)}/", "/:$1/");
    path = path.replaceAll("/\\{string:([^}]+)}/", "/:$1/");
    return path;
  }
}
