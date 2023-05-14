package com.example.catalog.web.route.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthHandlerSupplier implements Supplier<Handler<RoutingContext>> {

  private final AuthHandler authHandler;

  @Inject
  public AuthHandlerSupplier(AuthHandler authHandler) {
    this.authHandler = authHandler;
  }

  @Override
  public Handler<RoutingContext> get() {
    return authHandler;
  }
}
