/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.commons.kafka.consumer.MessageHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class ExampleMessageHandler implements MessageHandler {

  @Inject
  ExampleMessageHandler() {}

  @Override
  public String getTopic() {
    return "example";
  }

  @Override
  public void handle(KafkaConsumerRecord<String, Buffer> message) {
    log.info("received message: " + message.value().toString());
  }
}
