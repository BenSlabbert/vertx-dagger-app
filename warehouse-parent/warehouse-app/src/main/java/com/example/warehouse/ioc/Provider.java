/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.ioc;

import com.example.warehouse.repository.RepositoryModule;
import com.example.warehouse.service.ServiceModule;
import com.example.warehouse.verticle.WarehouseVerticle;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcApiModule;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcService;
import github.benslabbert.vertxdaggercommons.closer.CloserModule;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.jooq.StaticSqlDslContextModule;
import github.benslabbert.vertxdaggerstarter.reactivedbpool.PoolModule;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.sqlclient.Pool;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      StaticSqlDslContextModule.class,
      CloserModule.class,
      IamRpcApiModule.class,
      PoolModule.class,
      ServiceModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  Config config();

  ClosingService closingService();

  WarehouseRpcService warehouseRpcService();

  AuthenticationProvider iamRpcServiceAuthenticationProvider();

  Pool pool();

  WarehouseVerticle warehouseVerticle();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder httpConfig(Config.HttpConfig httpConfig);

    @BindsInstance
    Builder postgresConfig(Config.PostgresConfig postgresConfig);

    Provider build();
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(Pool pool, @Named("static") DSLContext dsl) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
