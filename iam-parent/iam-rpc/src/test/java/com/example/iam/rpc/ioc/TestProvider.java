/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.ioc;

import com.example.iam.rpc.service.ServiceModule;
import dagger.BindsInstance;
import dagger.Component;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.Vertx;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, Provider.EagerModule.class})
public interface TestProvider extends Provider {

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    TestProvider build();
  }
}
