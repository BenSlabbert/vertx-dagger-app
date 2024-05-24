/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ItemService;
import com.example.catalog.service.ServiceModule;
import com.example.catalog.verticle.ApiVerticle;
import com.example.catalog.web.WebModule;
import com.example.catalog.web.route.handler.AuthHandler;
import com.example.catalog.web.route.handler.HandlerModule;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.iam.rpc.api.IamRpcApiModule;
import com.example.starter.reactive.pool.PoolModule;
import com.example.starter.redis.RedisModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.vertxdaggercommons.closer.CloserModule;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.jooq.StaticSqlDslContextModule;
import github.benslabbert.vertxdaggercommons.mesage.Consumer;
import github.benslabbert.vertxdaggercommons.saga.SagaModule;
import io.vertx.core.Vertx;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      StaticSqlDslContextModule.class,
      PoolModule.class,
      RedisModule.class,
      CloserModule.class,
      RepositoryModule.class,
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
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(RedisAPI redisAPI, Pool pool, @Named("static") DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
