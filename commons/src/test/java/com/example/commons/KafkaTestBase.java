/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import com.example.commons.config.Config;
import com.example.commons.ioc.DaggerProvider;
import com.example.commons.ioc.Provider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(VertxExtension.class)
public abstract class KafkaTestBase {

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
            .kafkaProducerConfig(Config.KafkaProducerConfig.builder().clientId("clientId").build())
            .kafkaConsumerConfig(
                Config.KafkaConsumerConfig.builder()
                    .clientId("clientId")
                    .consumerGroup("consumerGroup")
                    .maxPollRecords(1)
                    .build())
            .build();

    provider = DaggerProvider.builder().vertx(vertx).kafkaConfig(kafkaConfig).build();
  }
}
