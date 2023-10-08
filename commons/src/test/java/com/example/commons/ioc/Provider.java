/* Licensed under Apache-2.0 2023. */
package com.example.commons.ioc;

import com.example.commons.config.Config;
import com.example.commons.kafka.KafkaModule;
import com.example.commons.kafka.producer.TopicCreatorFactory;
import com.example.commons.saga.SagaBuilder;
import com.example.commons.saga.SagaModule;
import com.google.protobuf.GeneratedMessageV3;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import javax.inject.Singleton;

@Singleton
@Component(modules = {SagaModule.class, KafkaModule.class})
public interface Provider {

  SagaBuilder sagaBuilder();

  KafkaConsumer<String, Buffer> consumer();

  KafkaProducer<String, GeneratedMessageV3> producer();

  TopicCreatorFactory topicCreatorFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder kafkaConfig(Config.KafkaConfig kafkaConfig);

    Provider build();
  }
}
