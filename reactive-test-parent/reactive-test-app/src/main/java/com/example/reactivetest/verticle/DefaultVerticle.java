/* Licensed under Apache-2.0 2024. */
package com.example.reactivetest.verticle;

import com.example.reactivetest.ioc.DaggerProvider;
import com.example.reactivetest.ioc.Provider;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private ApplicationVerticle applicationVerticle;
  private Provider dagger;

  private void init() {
    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);

    this.dagger = DaggerProvider.builder().vertx(vertx).config(config).build();
    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    applicationVerticle = dagger.applicationVerticle();
    applicationVerticle.init(vertx, context);
    applicationVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    applicationVerticle.stop(stopPromise);
  }
}
