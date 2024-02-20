/* Licensed under Apache-2.0 2024. */
package com.example.iam.verticle;

import com.example.commons.config.Config;
import com.example.iam.ioc.DaggerProvider;
import com.example.iam.ioc.Provider;
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
    Objects.requireNonNull(config.verticleConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .verticleConfig(config.verticleConfig())
            .build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    apiVerticle = dagger.apiVerticle();
    apiVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    apiVerticle.stop(stopPromise);
  }
}
