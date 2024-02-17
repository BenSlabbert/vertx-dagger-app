/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url.generator;

import com.example.codegen.generator.commons.GenerationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class PathParser {

  private PathParser() {}

  static List<Param> parse(String path) {
    List<Param> params = new ArrayList<>();
    getPairs(path, params);
    return params;
  }

  static void getPairs(String path, List<Param> params) {
    Set<String> names = new HashSet<>();
    int idx = path.indexOf('{');
    while (idx != -1) {
      int endIdx = path.indexOf('}');
      String param = path.substring(idx + 1, endIdx);
      String[] split = param.split(":");
      if (2 != split.length) {
        throw new GenerationException("illegal path parameter: " + param);
      }

      String type = split[0];
      String name = split[1];

      if (!names.add(name)) {
        throw new GenerationException("duplicate path parameter: " + name);
      }

      if (Objects.equals(type, "int")) {
        params.add(new Param(Type.INT, name));
      } else if (Objects.equals(type, "string")) {
        params.add(new Param(Type.STRING, name));
      } else if (Objects.equals(type, "long")) {
        params.add(new Param(Type.LONG, name));
      } else {
        throw new GenerationException("illegal path parameter type: " + type);
      }

      path = path.substring(endIdx + 1);
      idx = path.indexOf('{');
    }
  }

  record Param(Type type, String name) {}

  enum Type {
    INT,
    LONG,
    STRING
  }
}
