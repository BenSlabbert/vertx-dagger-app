/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.verticle;

import com.example.jdbc.service.JdbcService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import javax.inject.Inject;

public class JdbcVerticle extends AbstractVerticle {

  private final JdbcService jdbcService;

  @Inject
  JdbcVerticle(JdbcService jdbcService) {
    this.jdbcService = jdbcService;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    stopPromise.complete();
  }
}
