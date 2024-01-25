/* Licensed under Apache-2.0 2024. */
package com.example.catalog.web.route.handler;

import io.vertx.ext.web.RoutingContext;

public interface ItemHandler {

  void executeSaga(RoutingContext ctx);

  void nextPage(RoutingContext ctx, long fromId, int size);

  void previousPage(RoutingContext ctx, long fromId, int size);

  void suggest(RoutingContext ctx, String name);

  void findOne(RoutingContext ctx, long id);

  void deleteOne(RoutingContext ctx, long id);

  void create(RoutingContext ctx);

  void update(RoutingContext ctx, long id);
}
