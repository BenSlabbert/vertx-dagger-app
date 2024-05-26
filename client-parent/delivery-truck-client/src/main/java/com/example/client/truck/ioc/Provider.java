/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.ioc;

import com.example.client.truck.config.IamConfig;
import com.example.client.truck.service.JobService;
import com.example.client.truck.service.ServiceModule;
import com.example.client.truck.verticle.TruckVerticle;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcApiModule;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcServiceProviderFactory;
import github.benslabbert.vertxdaggerstarter.iamauthclient.IamAuthClientFactory;
import github.benslabbert.vertxdaggerstarter.iamauthclient.IamAuthClientModule;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      WarehouseRpcApiModule.class,
      IamAuthClientModule.class,
      Provider.EagerModule.class,
      ServiceModule.class
    })
public interface Provider {

  @Nullable Void init();

  IamAuthClientFactory iamAuthClientFactory();

  IamConfig iamConfig();

  WarehouseRpcServiceProviderFactory warehouseRpcServiceProviderFactory();

  JobService jobService();

  TruckVerticle truckVerticle();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder iamConfig(IamConfig config);

    Provider build();
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager() {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
