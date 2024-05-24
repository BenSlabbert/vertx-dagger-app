/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.verticle;

import com.example.iam.rpc.ioc.DaggerProvider;
import com.example.iam.rpc.ioc.Provider;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private RpcVerticle rpcVerticle;
  private Provider dagger;

  private void init() {
    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.httpConfig());

    this.dagger = DaggerProvider.builder().vertx(vertx).config(config).build();
    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    rpcVerticle = dagger.rpcVerticle();
    rpcVerticle.init(vertx, context);
    rpcVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    rpcVerticle.stop(stopPromise);
  }
}
