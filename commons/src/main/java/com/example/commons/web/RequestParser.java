/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;

public class RequestParser {

  private final MultiMap queryParams;
  private final Map<String, String> pathParams;

  private RequestParser(RoutingContext ctx) {
    this.queryParams = ctx.queryParams();
    this.pathParams = ctx.pathParams();
  }

  public static RequestParser create(RoutingContext ctx) {
    return new RequestParser(ctx);
  }

  public <T> T getQueryParam(String key, Parser<T> parser) {
    String string = queryParams.get(key);
    return parser.parse(string);
  }

  public <T> T getQueryParam(String key, T defaultValue, Parser<T> parser) {
    String string = queryParams.get(key);
    return parser.parse(string, defaultValue);
  }

  public <T> T getPathParam(String key, Parser<T> parser) {
    String string = pathParams.get(key);
    return parser.parse(string);
  }

  public <T> T getPathParam(String key, T defaultValue, Parser<T> parser) {
    String string = pathParams.get(key);
    return parser.parse(string, defaultValue);
  }
}
