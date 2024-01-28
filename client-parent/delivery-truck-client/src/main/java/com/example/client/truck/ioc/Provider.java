/* Licensed under Apache-2.0 2024. */
package com.example.client.truck.ioc;

import com.example.client.truck.config.IamConfig;
import com.example.starter.iam.auth.client.IamAuthClientFactory;
import com.example.starter.iam.auth.client.IamAuthClientModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(modules = {IamAuthClientModule.class, Provider.EagerModule.class})
public interface Provider {

  @Nullable Void init();

  IamAuthClientFactory iamAuthClientFactory();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder iamConfig(IamConfig config);

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
