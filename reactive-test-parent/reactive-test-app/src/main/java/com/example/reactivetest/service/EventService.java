package com.example.reactivetest.service;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class EventService {

  private static final String KAFKA_OUTBOX_SEND = "kafka.outbox.send";

  private final Vertx vertx;

  @Inject
  EventService(Vertx vertx) {
    this.vertx = vertx;
  }

  void publishKafkaOutboxEvent(long id) {
    vertx.eventBus().send(KAFKA_OUTBOX_SEND, id);
  }

  void consumeKafkaOutboxEvent(Handler<Long> consumer) {
    vertx.eventBus().consumer(KAFKA_OUTBOX_SEND, m -> consumer.handle((long) m.body()));
  }
}
