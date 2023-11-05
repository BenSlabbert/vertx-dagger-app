/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.producer;

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

import com.example.commons.config.Config;
import com.google.protobuf.GeneratedMessageV3;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import lombok.extern.java.Log;

@Log
@Module
public class KafkaProducerFactory {

  private static final AtomicInteger INCR = new AtomicInteger(0);

  private KafkaProducerFactory() {}

  @Provides
  public static KafkaProducer<String, GeneratedMessageV3> createProducer(
      Vertx vertx, Config.KafkaConfig kafkaConfig) {

    Map<String, String> config = new HashMap<>();

    config.put(BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers());
    config.put(
        KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    config.put(VALUE_SERIALIZER_CLASS_CONFIG, ProtobufSerializer.class.getCanonicalName());
    config.put(ACKS_CONFIG, "1");
    config.put(RETRIES_CONFIG, "1");
    config.put(MAX_BLOCK_MS_CONFIG, "5000");
    config.put(LINGER_MS_CONFIG, "250");
    config.put(REQUEST_TIMEOUT_MS_CONFIG, "2500");
    config.put(DELIVERY_TIMEOUT_MS_CONFIG, "5000");
    config.put(CLIENT_ID_CONFIG, kafkaConfig.producer().clientId()+"-"+INCR.getAndIncrement());

    return KafkaProducer.<String, GeneratedMessageV3>create(vertx, config)
        .exceptionHandler(err -> log.log(Level.SEVERE, "unhandled exception", err));
  }
}
