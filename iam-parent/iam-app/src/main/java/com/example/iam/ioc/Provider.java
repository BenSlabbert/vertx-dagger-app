/* Licensed under Apache-2.0 2023. */
package com.example.iam.ioc;

import com.example.commons.closer.CloserModule;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.iam.repository.RepositoryModule;
import com.example.iam.service.ServiceModule;
import com.example.iam.verticle.ApiVerticle;
import com.example.iam.web.route.handler.UserHandler;
import com.example.starter.redis.RedisModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.redis.client.RedisAPI;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      CloserModule.class,
      RepositoryModule.class,
      RedisModule.class,
      ServiceModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  Config config();

  RedisAPI redisAPI();

  ClosingService closingService();

  UserHandler userHandler();

  ApiVerticle apiVerticle();

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

    Provider build();
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(RedisAPI redisAPI) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
