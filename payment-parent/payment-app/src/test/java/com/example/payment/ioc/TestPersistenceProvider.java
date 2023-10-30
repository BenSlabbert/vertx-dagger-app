/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.config.Config;
import com.example.commons.kafka.KafkaModule;
import com.example.payment.config.ConfigModule;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.service.PaymentService;
import com.example.payment.service.ServiceModule;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import java.util.Map;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(modules = {ServiceModule.class, ConfigModule.class, KafkaModule.class})
public interface TestPersistenceProvider extends Provider {

  DSLContext dslContext();

  PaymentRepository paymentRepository();

  PaymentService paymentService();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder httpConfig(Config.HttpConfig httpConfig);

    @BindsInstance
    Builder redisConfig(Config.RedisConfig redisConfig);

    @BindsInstance
    Builder verticleConfig(Config.VerticleConfig verticleConfig);

    @BindsInstance
    Builder postgresConfig(Config.PostgresConfig postgresConfig);

    @BindsInstance
    Builder serviceRegistryConfig(Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig> map);

    @BindsInstance
    Builder kafkaConfig(Config.KafkaConfig kafkaConfig);

    TestPersistenceProvider build();
  }
}
