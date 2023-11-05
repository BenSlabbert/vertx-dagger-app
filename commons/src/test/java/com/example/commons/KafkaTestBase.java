/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import com.example.commons.config.Config;
import com.example.commons.ioc.DaggerProvider;
import com.example.commons.ioc.Provider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(VertxExtension.class)
public abstract class KafkaTestBase {

  private static final AtomicInteger counter = new AtomicInteger(0);

  private static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
          .withEnv("KAFKA_HEAP_OPTS", "-Xmx512M -Xms512M");

  static {
    kafka.start();
  }

  protected static Provider provider;

  @BeforeAll
  public static void before(Vertx vertx) {
    Config.KafkaConfig kafkaConfig =
        Config.KafkaConfig.builder()
            .bootstrapServers(kafka.getBootstrapServers())
            .producer(
                Config.KafkaProducerConfig.builder()
                    .clientId("producer-id-" + counter.get())
                    .build())
            .consumer(
                Config.KafkaConsumerConfig.builder()
                    .clientId("consumer-id-" + counter.get())
                    .consumerGroup("consumer-group-" + counter.get())
                    .maxPollRecords(1)
                    .build())
            .build();

    provider = DaggerProvider.builder().vertx(vertx).kafkaConfig(kafkaConfig).build();
  }
}
