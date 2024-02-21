/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.verticle;

import com.example.commons.config.Config;
import com.example.warehouse.ioc.DaggerProvider;
import com.example.warehouse.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private Provider dagger;
  private WarehouseVerticle warehouseVerticle;

  private void init() {
    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.httpConfig());
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
    warehouseVerticle = dagger.warehouseVerticle();
    warehouseVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    warehouseVerticle.stop(stopPromise);
  }
}
