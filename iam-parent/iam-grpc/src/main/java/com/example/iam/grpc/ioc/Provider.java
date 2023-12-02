/* Licensed under Apache-2.0 2023. */
package com.example.iam.grpc.ioc;

import com.example.commons.config.Config;
import com.example.iam.grpc.service.GrpcService;
import com.example.iam.grpc.service.ServiceModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
      Provider.EagerModule.class,
    })
public interface Provider {

  @Nullable Void init();

  GrpcService grpcService();

  Config config();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

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
