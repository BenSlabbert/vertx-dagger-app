/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.kafka.KafkaModule;
import com.example.payment.Main;
import com.example.payment.config.ConfigModule;
import com.example.payment.repository.RepositoryModule;
import com.example.payment.service.ServiceLifecycleManagement;
import com.example.payment.service.ServiceModule;
import com.example.payment.verticle.WorkerVerticle;
import com.google.protobuf.GeneratedMessageV3;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      Main.class,
      ServiceModule.class,
      ConfigModule.class,
      KafkaModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  WorkerVerticle provideNewWorkerVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  KafkaConsumer<String, Buffer> consumer();

  KafkaProducer<String, GeneratedMessageV3> producer();

  @Module
  class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(DataSource dataSource, DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
