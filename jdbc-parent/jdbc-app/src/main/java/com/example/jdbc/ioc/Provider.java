/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.ioc;

import com.example.jdbc.service.JdbcService;
import com.example.jdbc.service.NestedTransactionService;
import com.example.jdbc.service.ServiceModule;
import com.example.jdbc.service.TransactionService;
import com.example.jdbc.verticle.JdbcVerticle;
import com.example.starter.jdbc.pool.JdbcPoolModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.txmanager.PlatformTransactionManager;
import github.benslabbert.vertxdaggercommons.closer.CloserModule;
import github.benslabbert.vertxdaggercommons.closer.ClosingService;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.jooq.PreparedStatementDslContextModule;
import github.benslabbert.vertxdaggercommons.jooq.StaticSqlDslContextModule;
import github.benslabbert.vertxdaggercommons.transaction.blocking.TransactionManagerModule;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcTransactionManager;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcTransactionManagerModule;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      CloserModule.class,
      ServiceModule.class,
      TransactionManagerModule.class,
      PreparedStatementDslContextModule.class,
      StaticSqlDslContextModule.class,
      JdbcPoolModule.class,
      Provider.EagerModule.class,
      JdbcTransactionManagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  JdbcVerticle jdbcVerticle();

  JdbcService jdbcService();

  TransactionService transactionService();

  NestedTransactionService nestedTransactionService();

  ClosingService closingService();

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

  default void close() {
    try {
      PlatformTransactionManager.close();
    } catch (Exception e) {
      // do nothing
    }
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(
        JdbcTransactionManager jdbcTransactionManager,
        DataSource dataSource,
        @Named("prepared") DSLContext preparedDslContext,
        @Named("static") DSLContext staticDslContext) {
      // this eagerly builds any parameters specified and returns nothing
      PlatformTransactionManager.setTransactionManager(jdbcTransactionManager);
      return null;
    }
  }
}
