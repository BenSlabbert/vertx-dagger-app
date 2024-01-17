/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.verticle;

import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.iam.rpc.api.IamRpcService;
import com.example.iam.rpc.api.IamRpcServiceVertxProxyHandler;
import com.example.iam.rpc.ioc.DaggerProvider;
import com.example.iam.rpc.ioc.Provider;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RpcVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(RpcVerticle.class);

  private Provider dagger;
  private MessageConsumer<JsonObject> consumer;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting");
    init();

    vertx
        .eventBus()
        .addInboundInterceptor(
            ctx -> {
              log.info("inbound interceptor");
              ctx.next();
            })
        .addOutboundInterceptor(
            ctx -> {
              log.info("outbound interceptor");
              ctx.next();
            });

    this.consumer =
        new IamRpcServiceVertxProxyHandler(vertx, dagger.iamRpcService())
            .register(vertx.eventBus(), IamRpcService.ADDRESS)
            .exceptionHandler(err -> log.error("failed to register iam rpc service", err))
            .setMaxBufferedMessages(100)
            .fetch(10)
            .exceptionHandler(err -> log.error("exception in event bus", err))
            .endHandler(ignore -> log.info("end handler"));

    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    consumer
        .unregister()
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                log.info("stopped");
                stopPromise.complete();
              } else {
                log.error("failed to stop", ar.cause());
                stopPromise.fail(ar.cause());
              }
            });
  }

  private void init() {
    JsonObject cfg = config();
    Config config = ParseConfig.get(cfg);

    Objects.requireNonNull(vertx);
    Objects.requireNonNull(config);

    log.info("create dagger");
    this.dagger = DaggerProvider.builder().vertx(vertx).config(config).build();
    log.info("init dagger deps");
    this.dagger.init();
    log.info("dagger init complete");
  }
}
