/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.consumer;

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;

import com.example.commons.config.Config;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.kafka.clients.producer.ProducerConfig;

@Log
@Module
public class KafkaConsumerFactory {

  private KafkaConsumerFactory() {}

  @Provides
  public static KafkaConsumer<String, Buffer> create(Vertx vertx, Config.KafkaConfig kafkaConfig) {

    Map<String, String> config = new HashMap<>();
    String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    String valueDeserializer = "io.vertx.kafka.client.serialization.BufferDeserializer";

    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers());
    config.put(KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
    config.put(VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
    config.put(GROUP_ID_CONFIG, kafkaConfig.kafkaConsumerConfig().consumerGroup());
    config.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
    config.put(
        MAX_POLL_RECORDS_CONFIG,
        Integer.toString(kafkaConfig.kafkaConsumerConfig().maxPollRecords()));
    config.put(CLIENT_ID_CONFIG, kafkaConfig.kafkaConsumerConfig().clientId());

    return KafkaConsumer.<String, Buffer>create(vertx, config)
        .exceptionHandler(err -> log.log(Level.SEVERE, "unhandled exception", err));
  }
}
