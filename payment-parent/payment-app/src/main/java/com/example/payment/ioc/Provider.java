/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.kafka.KafkaModule;
import com.example.payment.Main;
import com.example.payment.config.ConfigModule;
import com.example.payment.service.ServiceLifecycleManagement;
import com.example.payment.service.ServiceModule;
import com.example.payment.verticle.ApiVerticle;
import com.google.protobuf.GeneratedMessageV3;
import dagger.Component;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import javax.inject.Singleton;

@Singleton
@Component(modules = {Main.class, ServiceModule.class, ConfigModule.class, KafkaModule.class})
public interface Provider {

  ApiVerticle provideNewApiVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  KafkaConsumer<String, Buffer> consumer();

  KafkaProducer<String, GeneratedMessageV3> producer();
}
