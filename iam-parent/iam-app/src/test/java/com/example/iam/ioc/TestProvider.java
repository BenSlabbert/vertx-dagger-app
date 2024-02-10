/* Licensed under Apache-2.0 2023. */
package com.example.iam.ioc;

import com.example.commons.config.Config;
import com.example.iam.repository.RepositoryModule;
import com.example.iam.service.ServiceModule;
import com.example.iam.service.TokenService;
import com.example.starter.redis.RedisModule;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      RepositoryModule.class,
      RedisModule.class,
      Provider.EagerModule.class,
      ServiceModule.class
    })
public interface TestProvider extends Provider {

  TokenService tokenService();

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
    Builder redisConfig(Config.RedisConfig redisConfig);

    TestProvider build();
  }
}
