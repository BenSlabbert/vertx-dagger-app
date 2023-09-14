/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web.route.handler;

import dagger.Binds;
import dagger.Module;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.util.function.Supplier;

@Module
public interface HandlerModule {

  @Binds
  Supplier<Handler<RoutingContext>> createAuthHandler(AuthHandlerSupplier authHandler);
}
