/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.config.ConfigModule;
import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceModule;
import com.example.commons.config.Config;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      // do not use Main.class
      ServiceModule.class,
      RepositoryModule.class,
      ConfigModule.class,
      // use builder below instead of the real IAM one
    })
public interface TestProvider extends Provider {

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder authenticationIntegration(AuthenticationIntegration authenticationIntegration);

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
    Builder serviceRegistryConfig(Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig> map);

    TestProvider build();
  }
}
