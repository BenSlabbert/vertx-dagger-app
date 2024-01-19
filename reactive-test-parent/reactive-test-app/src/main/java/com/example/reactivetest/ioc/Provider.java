/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.ioc;

import com.example.commons.config.Config;
import com.example.reactivetest.config.ConfigModule;
import com.example.reactivetest.service.ServiceLifecycleManagement;
import com.example.reactivetest.service.ServiceModule;
import com.example.reactivetest.web.handler.PersonHandler;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(modules = {ConfigModule.class, ServiceModule.class, Provider.EagerModule.class})
public interface Provider {

  @Nullable Void init();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  Config config();

  PersonHandler personHandler();

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
    @Nullable static Void provideEager(Pool pool, DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
