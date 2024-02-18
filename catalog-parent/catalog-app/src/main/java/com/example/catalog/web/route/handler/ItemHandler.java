/* Licensed under Apache-2.0 2024. */
package com.example.catalog.web.route.handler;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public interface ItemHandler {

  void configureRoutes(Router router);

  void executeSaga(RoutingContext ctx);

  void nextPage(RoutingContext ctx);

  void previousPage(RoutingContext ctx);

  void suggest(RoutingContext ctx);

  void findOne(RoutingContext ctx);

  void deleteOne(RoutingContext ctx);

  void create(RoutingContext ctx);

  void update(RoutingContext ctx);
}
