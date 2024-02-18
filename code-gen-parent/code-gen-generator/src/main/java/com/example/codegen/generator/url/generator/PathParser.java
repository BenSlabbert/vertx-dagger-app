/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url.generator;

import com.example.codegen.generator.commons.GenerationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class PathParser {

  private PathParser() {}

  static ParseResult parse(String path) {
    Set<String> names = new HashSet<>();
    List<Param> pathParams = new ArrayList<>();
    List<Param> queryParams = new ArrayList<>();

    int queryStart = path.indexOf('?');
    if (-1 != queryStart) {
      String queryString = path.substring(queryStart + 1);
      path = path.substring(0, queryStart);
      updateParams(queryString, queryParams, names);
    }

    updateParams(path, pathParams, names);
    return new ParseResult(queryParams, pathParams);
  }

  static void updateParams(String path, List<Param> params, Set<String> names) {
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

      switch (type) {
        case "int" -> params.add(new Param(Type.INT, name));
        case "string" -> params.add(new Param(Type.STRING, name));
        case "long" -> params.add(new Param(Type.LONG, name));
        case "boolean" -> params.add(new Param(Type.BOOLEAN, name));
        case "float" -> params.add(new Param(Type.FLOAT, name));
        case "double" -> params.add(new Param(Type.DOUBLE, name));
        case "ts" -> params.add(new Param(Type.TIMESTAMP, name));
        case null, default -> throw new GenerationException("illegal path parameter type: " + type);
      }

      path = path.substring(endIdx + 1);
      idx = path.indexOf('{');
    }
  }

  record ParseResult(List<Param> queryParams, List<Param> pathParams) {}

  record Param(Type type, String name) {}

  enum Type {
    INT,
    LONG,
    BOOLEAN,
    FLOAT,
    DOUBLE,
    TIMESTAMP,
    STRING
  }
}
