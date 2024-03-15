/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.verticle;

import com.example.commons.config.Config;
import com.example.jtehtmx.ioc.DaggerProvider;
import com.example.jtehtmx.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public class DefaultVerticle extends AbstractVerticle {

  private JteHtmxVerticle jteHtmxVerticle;
  private Provider dagger;

  private void init() {
    JsonObject cfg = config();
    Config config = Config.fromJson(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.httpConfig());

    this.dagger =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .build();

    this.dagger.init();
  }

  @Override
  public void start(Promise<Void> startPromise) {
    init();
    jteHtmxVerticle = dagger.jteHtmxVerticle();
    jteHtmxVerticle.init(vertx, context);
    jteHtmxVerticle.start(startPromise);
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    jteHtmxVerticle.stop(stopPromise);
  }
}
