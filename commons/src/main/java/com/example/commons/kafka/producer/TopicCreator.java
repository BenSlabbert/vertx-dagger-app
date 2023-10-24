/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.producer;

import com.example.commons.config.Config;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.admin.NewTopic;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import lombok.extern.java.Log;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.errors.TopicExistsException;

@Log
public class TopicCreator {

  private final Config.KafkaConfig kafkaConfig;
  private final String topic;
  private final Vertx vertx;

  @AssistedInject
  TopicCreator(Vertx vertx, Config.KafkaConfig kafkaConfig, @Assisted String topic) {
    this.vertx = vertx;
    this.kafkaConfig = kafkaConfig;
    this.topic = topic;
  }

  public Future<Void> create() {
    return create(1, (short) 1);
  }

  public Future<Void> create(int partitions, short replicationFactor) {
    Properties config = new Properties();
    config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers());

    KafkaAdminClient client = KafkaAdminClient.create(vertx, config);

    return client
        .createTopics(List.of(new NewTopic(topic, partitions, replicationFactor)))
        .recover(
            err -> {
              if (err instanceof TopicExistsException) {
                log.warning("topic %s already exists".formatted(topic));
                return Future.succeededFuture();
              }
              log.log(Level.SEVERE, "failed to create topics", err);
              return Future.failedFuture(err);
            })
        .eventually(
            ignore ->
                client
                    .close()
                    .onFailure(err -> log.log(Level.SEVERE, "failed to close client", err))
                    .onSuccess(v -> log.info("closed kafka admin client")));
  }
}
