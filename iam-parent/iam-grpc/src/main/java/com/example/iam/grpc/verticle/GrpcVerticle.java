/* Licensed under Apache-2.0 2023. */
package com.example.iam.grpc.verticle;

import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.iam.grpc.ioc.DaggerProvider;
import com.example.iam.grpc.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GrpcVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(GrpcVerticle.class);

  private Provider dagger;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting");
    init();

    int port = dagger.config().grpcConfig().port();

    vertx
        .createHttpServer(new HttpServerOptions().setPort(port).setHost("0.0.0.0"))
        .requestHandler(dagger.grpcService().getGrpcServer())
        .listen(
            res -> {
              if (res.succeeded()) {
                log.info("started grpc server");
                startPromise.complete();
              } else {
                log.error("failed to start", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    stopPromise.complete();
  }

  private void init() {
    JsonObject cfg = config();
    Config config = ParseConfig.get(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.redisConfig());

    log.info("GrpcVerticle constructor");
    this.dagger = DaggerProvider.builder().vertx(vertx).config(config).build();
    this.dagger.init();
  }
}
