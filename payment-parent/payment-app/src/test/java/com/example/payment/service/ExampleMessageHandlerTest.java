/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import static com.example.commons.kafka.common.Headers.SAGA_ID_HEADER;
import static com.example.commons.kafka.common.Headers.SAGA_ROLLBACK_HEADER;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.payment.MockPersistenceTest;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.impl.KafkaConsumerRecordImpl;
import io.vertx.kafka.client.producer.KafkaHeader;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class ExampleMessageHandlerTest extends MockPersistenceTest {

  @Test
  void getTopic() {
    ExampleMessageHandler exampleMessageHandler = provider.exampleMessageHandler();
    assertThat(exampleMessageHandler.getTopic()).isEqualTo("Saga.Catalog.CreatePayment");
  }

  @Test
  void rollback() {
    ExampleMessageHandler exampleMessageHandler = provider.exampleMessageHandler();

    ConsumerRecord<String, Buffer> stringStringConsumerRecord =
        new ConsumerRecord<>("", 0, 0, "", null);
    KafkaConsumerRecordImpl<String, Buffer> message =
        new KafkaConsumerRecordImpl<>(stringStringConsumerRecord);
    message.headers().add(KafkaHeader.header(SAGA_ID_HEADER, Buffer.buffer("sagaId")));
    message.headers().add(KafkaHeader.header(SAGA_ROLLBACK_HEADER, Buffer.buffer()));

    exampleMessageHandler.handle(message);
  }
}
