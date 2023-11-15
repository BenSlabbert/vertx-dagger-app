/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.Main;
import com.example.catalog.config.ConfigModule;
import com.example.catalog.integration.IntegrationModule;
import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceLifecycleManagement;
import com.example.catalog.service.ServiceModule;
import com.example.catalog.verticle.ApiVerticle;
import com.example.commons.mesage.Consumer;
import com.example.commons.saga.SagaModule;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.pgclient.PgPool;
import io.vertx.redis.client.RedisAPI;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      RepositoryModule.class,
      Main.class,
      ConfigModule.class,
      IntegrationModule.class,
      MapperModule.class,
      SagaModule.class,
      ServiceModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  ApiVerticle provideNewApiVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  Set<Consumer> consumers();

  @Module
  class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(RedisAPI redisAPI, PgPool pgPool, DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
