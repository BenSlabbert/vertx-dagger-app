/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.config.Config;
import dagger.BindsInstance;
import io.vertx.core.Vertx;
import java.util.Map;

public interface BaseBuilder<
    BUILDER extends BaseBuilder<?, ? extends Provider>, PROVIDER extends Provider> {

  @BindsInstance
  BUILDER vertx(Vertx vertx);

  @BindsInstance
  BUILDER config(Config config);

  @BindsInstance
  BUILDER httpConfig(Config.HttpConfig httpConfig);

  @BindsInstance
  BUILDER verticleConfig(Config.VerticleConfig verticleConfig);

  @BindsInstance
  BUILDER postgresConfig(Config.PostgresConfig postgresConfig);

  @BindsInstance
  BUILDER serviceRegistryConfig(Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig> map);

  @BindsInstance
  BUILDER kafkaConfig(Config.KafkaConfig kafkaConfig);

  PROVIDER build();
}
