/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.ioc;

import com.example.reactivetest.service.ServiceModule;
import com.example.reactivetest.verticle.ApplicationVerticle;
import com.example.reactivetest.web.handler.HandlerModule;
import com.example.reactivetest.web.handler.PersonHandler;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.vertxdaggercommons.closer.CloserModule;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.jooq.StaticSqlDslContextModule;
import github.benslabbert.vertxdaggerstarter.reactivedbpool.PoolModule;
import io.vertx.core.Vertx;
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
      PoolModule.class,
      CloserModule.class,
      ServiceModule.class,
      HandlerModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  ClosingService closingService();

  Config config();

  PersonHandler personHandler();

  ApplicationVerticle applicationVerticle();

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
    @Nullable static Void provideEager(Pool pool, @Named("static") DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
