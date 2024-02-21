/* Licensed under Apache-2.0 2024. */
package com.example.payment.verticle;

import com.example.commons.config.Config;
import com.example.payment.ioc.DaggerProvider;
import com.example.payment.ioc.Provider;
import com.example.payment.service.TestingScopeService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.ThreadingModel;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private WorkerVerticle workerVerticle;
  private Provider dagger;

  private void init() {
    Context orCreateContext = vertx.getOrCreateContext();
    boolean workerContext = orCreateContext.isWorkerContext();
    ThreadingModel threadingModel = orCreateContext.threadingModel();
    if (!workerContext && threadingModel != ThreadingModel.VIRTUAL_THREAD) {
      throw new IllegalStateException("not running in a worker/virtual thread context");
    }

    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.postgresConfig());
    Objects.requireNonNull(config.httpConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .postgresConfig(config.postgresConfig())
            .build();

    this.dagger.init();
    TestingScopeService testingScopeService = this.dagger.testingScopeService();
    testingScopeService.handle();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    workerVerticle = dagger.workerVerticle();
    workerVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    workerVerticle.stop(stopPromise);
  }
}
