/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.commons.kafka.consumer.MessageHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class KafkaService implements AutoCloseable {

  private final KafkaConsumer<String, Buffer> consumer;
  private final Map<String, MessageHandler> handlerMap;

  @Inject
  KafkaService(KafkaConsumer<String, Buffer> consumer, Set<MessageHandler> handlers) {
    this.consumer = consumer;
    this.handlerMap =
        handlers.stream().collect(Collectors.toMap(MessageHandler::getTopic, Function.identity()));

    long handlersForTopic = handlerMap.keySet().size();
    long numberOfTopicHandlers = handlers.stream().map(MessageHandler::getTopic).count();

    if (handlersForTopic != numberOfTopicHandlers) {
      log.severe("duplicate topic handlers");
      throw new RuntimeException("duplicate topic handlers");
    }

    this.consumer
        .handler(this::handle)
        .subscribe("example")
        .onFailure(err -> log.severe(err.getMessage()))
        .onSuccess(ignore -> log.info("subscribed to topic: example"));
  }

  private void handle(KafkaConsumerRecord<String, Buffer> message) {
    MessageHandler messageHandler = handlerMap.get(message.topic());

    if (null == messageHandler) {
      // no handler for this topic
      log.severe("no handler for topic: " + message.topic());
      return;
    }

    messageHandler.handle(message);
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() throws InterruptedException {
    if (null == consumer) return;

    CountDownLatch latch = new CountDownLatch(1);
    consumer
        .close()
        .onComplete(
            r -> {
              if (r.failed()) {
                System.err.println("closing kafka consumer failed: " + r.cause());
              }
              latch.countDown();
            });

    latch.await();
  }
}
