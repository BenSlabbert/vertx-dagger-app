package com.example.shop.verticle;

import com.example.shop.service.ItemEventStreamConsumer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
public class ConsumerVerticle extends AbstractVerticle {

  // injecting here as we need the consumer to start
  private final ItemEventStreamConsumer itemEventStreamConsumer;

  @Inject
  public ConsumerVerticle(ItemEventStreamConsumer itemEventStreamConsumer) {
    this.itemEventStreamConsumer = itemEventStreamConsumer;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    startPromise.complete();
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
