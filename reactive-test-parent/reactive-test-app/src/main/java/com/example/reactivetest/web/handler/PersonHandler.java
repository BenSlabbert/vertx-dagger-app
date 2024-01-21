/* Licensed under Apache-2.0 2024. */
package com.example.reactivetest.web.handler;

import io.vertx.ext.web.RoutingContext;

public interface PersonHandler {

  void create(RoutingContext ctx);

  void getAll(RoutingContext ctx);

  void sse(RoutingContext ctx);
}
