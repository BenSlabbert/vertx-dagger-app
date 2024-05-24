/* Licensed under Apache-2.0 2024. */
package com.example.iam.verticle;

import com.example.iam.ioc.DaggerProvider;
import com.example.iam.ioc.Provider;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private ApiVerticle apiVerticle;
  private Provider dagger;

  private void init() {
    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.httpConfig());
    Objects.requireNonNull(config.redisConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    apiVerticle = dagger.apiVerticle();
    apiVerticle.init(vertx, context);
    apiVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    apiVerticle.stop(stopPromise);
  }
}
