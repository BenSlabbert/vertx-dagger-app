/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.config.Config;
import com.example.commons.kafka.KafkaModule;
import com.example.payment.config.ConfigModule;
import com.example.payment.repository.RepositoryModule;
import com.example.payment.service.KafkaConsumerService;
import com.example.payment.service.ServiceLifecycleManagement;
import com.example.payment.service.ServiceModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
      ConfigModule.class,
      KafkaModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  DataSource dataSource();

  KafkaConsumerService kafkaConsumerService();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder httpConfig(Config.HttpConfig httpConfig);

    @BindsInstance
    Builder verticleConfig(Config.VerticleConfig verticleConfig);

    @BindsInstance
    Builder postgresConfig(Config.PostgresConfig postgresConfig);

    @BindsInstance
    Builder serviceRegistryConfig(Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig> map);

    @BindsInstance
    Builder kafkaConfig(Config.KafkaConfig kafkaConfig);

    Provider build();
  }

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
