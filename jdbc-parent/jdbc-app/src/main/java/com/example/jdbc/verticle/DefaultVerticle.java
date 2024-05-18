/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.verticle;

import com.example.commons.config.Config;
import com.example.jdbc.ioc.DaggerProvider;
import com.example.jdbc.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private Provider dagger;
  private JdbcVerticle jdbcVerticle;

  private void init() {
    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.postgresConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .postgresConfig(config.postgresConfig())
            .build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    jdbcVerticle = dagger.jdbcVerticle();
    jdbcVerticle.init(vertx, context);
    jdbcVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    jdbcVerticle.stop(stopPromise);
  }
}
