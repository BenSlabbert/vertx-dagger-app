/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.commons.closer.CloserModule;
import com.example.commons.closer.ClosingService;
import com.example.commons.config.Config;
import com.example.commons.jooq.DataSourceDslContextModule;
import com.example.commons.mesage.Consumer;
import com.example.payment.repository.RepositoryModule;
import com.example.payment.scope.TransactionModule;
import com.example.payment.service.ServiceModule;
import com.example.payment.service.TestingScopeService;
import com.example.payment.verticle.WorkerVerticle;
import com.example.starter.jdbc.pool.JdbcPoolModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      DataSourceDslContextModule.class,
      JdbcPoolModule.class,
      CloserModule.class,
      ServiceModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class,
      TransactionModule.class
    })
public interface Provider {

  @Nullable Void init();

  DataSource dataSource();

  Set<Consumer> consumers();

  ClosingService closingService();

  TestingScopeService testingScopeService();

  WorkerVerticle workerVerticle();

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
    @Nullable static Void provideEager(DataSource dataSource, DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
