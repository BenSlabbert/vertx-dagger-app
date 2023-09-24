/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.integration.AuthenticationIntegration;
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
  BUILDER redisConfig(Config.RedisConfig redisConfig);

  @BindsInstance
  BUILDER verticleConfig(Config.VerticleConfig verticleConfig);

  @BindsInstance
  BUILDER serviceRegistryConfig(Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig> map);

  @BindsInstance
  BUILDER authenticationIntegration(AuthenticationIntegration authenticationIntegration);

  PROVIDER build();
}
