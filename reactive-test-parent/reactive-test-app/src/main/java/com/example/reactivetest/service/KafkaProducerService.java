package com.example.reactivetest.service;

import static com.example.reactivetest.config.KafkaTopics.TOPIC;
import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.LINGER_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.RETRIES_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import com.example.reactivetest.proto.v1.Person;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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
import org.apache.kafka.common.errors.TopicExistsException;

@Log
@Singleton
public class KafkaProducerService implements AutoCloseable {

  private final KafkaProducer<String, Buffer> producer;

  @Inject
  public KafkaProducerService(Vertx vertx) {
    Map<String, String> config = new HashMap<>();
    config.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
    config.put(
        KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    config.put(
        VALUE_SERIALIZER_CLASS_CONFIG, "io.vertx.kafka.client.serialization.BufferSerializer");
    config.put(ACKS_CONFIG, "1");
    config.put(RETRIES_CONFIG, "1");
    config.put(MAX_BLOCK_MS_CONFIG, "5000");
    config.put(LINGER_MS_CONFIG, "250");
    config.put(REQUEST_TIMEOUT_MS_CONFIG, "2500");
    config.put(DELIVERY_TIMEOUT_MS_CONFIG, "5000");
    config.put(CLIENT_ID_CONFIG, "producer.id");

    log.info("creating kafka producer");
    this.producer =
        KafkaProducer.<String, Buffer>create(vertx, config)
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

  public Future<RecordMetadata> emitPersonCreated(
      KafkaProducerRecord<String, Person> producerRecord) {
    byte[] bytes = producerRecord.value().toByteArray();
    Buffer buffer = Buffer.buffer(bytes);

    KafkaProducerRecord<String, Buffer> msg =
        KafkaProducerRecord.create(
            producerRecord.topic(), producerRecord.key(), buffer, producerRecord.partition());
    return producer.send(msg);
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() throws Exception {
    if (null == producer) {
      return;
    }

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
