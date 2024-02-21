/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.config.ConfigModule;
import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ItemService;
import com.example.catalog.service.ServiceModule;
import com.example.catalog.verticle.ApiVerticle;
import com.example.catalog.web.WebModule;
import com.example.catalog.web.route.handler.AuthHandler;
import com.example.catalog.web.route.handler.HandlerModule;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.closer.CloserModule;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.commons.mesage.Consumer;
import com.example.commons.saga.SagaModule;
import com.example.iam.rpc.api.IamRpcApiModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      CloserModule.class,
      RepositoryModule.class,
      ConfigModule.class,
      MapperModule.class,
      SagaModule.class,
      ServiceModule.class,
      IamRpcApiModule.class,
      HandlerModule.class,
      WebModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  Set<Consumer> consumers();

  AuthHandler authHandler();

  Config config();

  ItemHandler itemHandler();

  ItemService itemService();

  Pool pool();

  RedisAPI redisAPI();

  ApiVerticle apiVerticle();

  ClosingService closingService();

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
    Builder postgresConfig(Config.PostgresConfig postgresConfig);

    Provider build();
  }

  @Module
  class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(RedisAPI redisAPI, Pool pool, DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
