/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.verticle;

import com.example.client.truck.config.IamConfig;
import com.example.client.truck.ioc.DaggerProvider;
import com.example.client.truck.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private Provider dagger;
  private TruckVerticle truckVerticle;

  private void init() {
    JsonObject cfg = config();
    IamConfig iamConfig = IamConfig.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(iamConfig);

    this.dagger = DaggerProvider.builder().vertx(vertx).iamConfig(iamConfig).build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    truckVerticle = dagger.truckVerticle();
    truckVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    truckVerticle.stop(stopPromise);
  }
}
