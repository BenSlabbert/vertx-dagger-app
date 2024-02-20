/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.ioc;

import com.example.commons.closer.CloserModule;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.iam.rpc.api.IamRpcApiModule;
import com.example.iam.rpc.api.IamRpcServiceAuthenticationProvider;
import com.example.starter.reactive.pool.PoolModule;
import com.example.warehouse.rpc.api.WarehouseRpcService;
import com.example.warehouse.service.ServiceModule;
import com.example.warehouse.verticle.WarehouseVerticle;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      CloserModule.class,
      IamRpcApiModule.class,
      PoolModule.class,
      ServiceModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  Config config();

  ClosingService closingService();

  WarehouseRpcService warehouseRpcService();

  IamRpcServiceAuthenticationProvider iamRpcServiceAuthenticationProvider();

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

    @BindsInstance
    Builder verticleConfig(Config.VerticleConfig verticleConfig);

    Provider build();
  }

  @Module
  class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager() {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
