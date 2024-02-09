/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.verticle;

import com.example.client.truck.config.IamConfig;
import com.example.client.truck.config.WarehouseConfig;
import com.example.client.truck.ioc.DaggerProvider;
import com.example.client.truck.ioc.Provider;
import com.example.starter.iam.auth.client.IamAuthClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class TruckVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(TruckVerticle.class);

  private Provider dagger;

  private void init() {
    JsonObject cfg = config();
    IamConfig iamConfig = IamConfig.fromJson(cfg);
    WarehouseConfig warehouseConfig = WarehouseConfig.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(iamConfig);
    Objects.requireNonNull(warehouseConfig);

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .iamConfig(iamConfig)
            .warehouseConfig(warehouseConfig)
            .build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));
    init();

    IamConfig iamConfig = dagger.iamConfig();
    WarehouseConfig warehouseConfig = dagger.warehouseConfig();

    IamAuthClient iamAuthClient =
        dagger.iamAuthClientFactory().create("http://127.0.0.1/api", 8080);

    log.info("starting TruckVerticle");

    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    stopPromise.complete();
  }
}
