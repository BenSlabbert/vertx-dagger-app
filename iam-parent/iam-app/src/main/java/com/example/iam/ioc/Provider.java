/* Licensed under Apache-2.0 2023. */
package com.example.iam.ioc;

import com.example.commons.config.Config;
import com.example.iam.repository.RepositoryModule;
import com.example.iam.repository.UserRepository;
import com.example.iam.service.ServiceLifecycleManagement;
import com.example.iam.service.ServiceModule;
import com.example.iam.web.route.handler.UserHandler;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(modules = {RepositoryModule.class, ServiceModule.class, Provider.EagerModule.class})
public interface Provider {

  @Nullable Void init();

  Config config();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  UserHandler userHandler();

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

    Provider build();
  }

  @Module
  class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(UserRepository redisDB) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
