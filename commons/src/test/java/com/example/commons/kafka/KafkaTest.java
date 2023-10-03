/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.commons.Protobuf;
import com.example.commons.config.Config;
import com.example.commons.kafka.consumer.KafkaConsumerFactory;
import com.example.commons.kafka.producer.KafkaProducerFactory;
import com.example.commons.kafka.proto.v1.Proto;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.KafkaHeader;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(VertxExtension.class)
class KafkaTest {

  private static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

  static {
    kafka.start();
  }

  @Test
  @SneakyThrows
  void test(Vertx vertx, VertxTestContext testContext) {
    Config.KafkaConfig kafkaConfig =
        Config.KafkaConfig.builder()
            .bootstrapServers(kafka.getBootstrapServers())
            .kafkaProducerConfig(Config.KafkaProducerConfig.builder().clientId("clientId").build())
            .kafkaConsumerConfig(
                Config.KafkaConsumerConfig.builder()
                    .clientId("clientId")
                    .consumerGroup("consumerGroup")
                    .maxPollRecords(1)
                    .build())
            .build();

    KafkaProducerFactory.createTopic(vertx, "test")
        .onFailure(testContext::failNow)
        .onSuccess(ignore -> System.err.println("created topics successfully"));

    Proto test = Proto.newBuilder().setId(1L).setName("name").build();

    KafkaProducerRecord<String, GeneratedMessageV3> message =
        KafkaProducerRecord.create("test", "key", test, 0);

    message.addHeader("test-header", "header value");

    KafkaProducerFactory.createProducer(vertx, kafkaConfig)
        .send(message)
        .onFailure(testContext::failNow)
        .onSuccess(metadata -> System.err.println("sent to kafka successfully: " + metadata));

    CountDownLatch latch = new CountDownLatch(1);
    KafkaConsumerFactory.create(vertx, kafkaConfig)
        .handler(r -> handle(r, latch))
        .subscribe("test")
        .onFailure(testContext::failNow)
        .onSuccess(ignore -> System.err.println("subscribed successfully"));

    latch.await();
    testContext.completeNow();
  }

  private void handle(KafkaConsumerRecord<String, Buffer> r, CountDownLatch latch) {
    String key = r.key();
    List<KafkaHeader> headers = r.headers();
    Buffer value = r.value();

    Proto parse = Protobuf.parse(value.getBytes(), Proto.getDefaultInstance());

    assertThat(parse).isNotNull();
    int partition = r.partition();
    long offset = r.offset();
    String topic = r.topic();
    latch.countDown();
  }
}
