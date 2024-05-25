/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.ioc;

import com.example.iam.rpc.service.ServiceModule;
import com.example.iam.rpc.verticle.RpcVerticle;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, Provider.EagerModule.class})
public interface Provider {

  @Nullable Void init();

  IamRpcService iamRpcService();

  Config config();

  RpcVerticle rpcVerticle();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

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
