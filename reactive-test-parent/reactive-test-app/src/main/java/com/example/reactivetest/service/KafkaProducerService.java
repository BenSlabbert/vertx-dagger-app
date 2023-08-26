package com.example.reactivetest.service;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.kafka.client.producer.RecordMetadata;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.TopicExistsException;

@Log
@Singleton
public class KafkaProducerService implements AutoCloseable {

  private static final String TOPIC = "TOPIC";

  private final KafkaProducer<String, String> producer;

  @Inject
  public KafkaProducerService(Vertx vertx) {
    Map<String, String> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    config.put(
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    config.put(
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
        "org.apache.kafka.common.serialization.StringSerializer");
    config.put(ProducerConfig.ACKS_CONFIG, "1");
    config.put(ProducerConfig.RETRIES_CONFIG, "1");
    config.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "5000");
    config.put(ProducerConfig.LINGER_MS_CONFIG, "250");
    config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "2500");
    config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "5000");
    config.put(ProducerConfig.CLIENT_ID_CONFIG, "producer.id");

    log.info("creating kafka producer");
    this.producer =
        KafkaProducer.<String, String>create(vertx, config)
            .exceptionHandler(err -> log.log(Level.SEVERE, "unhandled exception", err));
    createTopics(vertx);
  }

  private void createTopics(Vertx vertx) {
    Properties config = new Properties();
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");

    KafkaAdminClient.create(vertx, config)
        .createTopics(List.of(new NewTopic(TOPIC, 1, (short) 1)))
        .onSuccess(v -> log.info("created topics"))
        .onFailure(
            err -> {
              if (err instanceof TopicExistsException) {
                log.info("topic already created");
                return;
              }
              log.log(Level.SEVERE, "failed to create topics", err);
            });
  }

  public Future<RecordMetadata> write() {
    log.info("writing to kafka");
    KafkaProducerRecord<String, String> record =
        KafkaProducerRecord.create(TOPIC, "key", "value", 0);

    return producer
        .send(record)
        .onSuccess(
            v -> {
              log.info("write success");
              log.info("getTopic: " + v.getTopic());
              log.info("getPartition: " + v.getPartition());
              log.info("getOffset: " + v.getOffset());
              log.info("getTimestamp: " + v.getTimestamp());
            })
        .onFailure(err -> log.log(Level.SEVERE, "failed to write", err));
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() throws Exception {
    if (null == producer) return;

    CountDownLatch latch = new CountDownLatch(1);
    System.err.println("closing kafka producer");
    producer
        .close()
        .onComplete(
            r -> {
              if (r.failed()) {
                System.err.println("failed to close pool: " + r.cause());
              }
              latch.countDown();
            });

    latch.await();
  }
}
