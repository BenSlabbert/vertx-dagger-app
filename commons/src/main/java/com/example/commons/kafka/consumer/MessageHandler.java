/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.consumer;

import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;

public interface MessageHandler {

  String getTopic();

  void handle(KafkaConsumerRecord<String, Buffer> message);
}
