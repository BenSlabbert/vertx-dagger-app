/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.ioc;

import com.example.commons.config.Config;
import com.example.commons.jooq.PreparedStatementDslContextModule;
import com.example.commons.jooq.StaticSqlDslContextModule;
import com.example.commons.transaction.blocking.TransactionManagerModule;
import com.example.commons.transaction.blocking.jdbc.JdbcTransactionManager;
import com.example.commons.transaction.blocking.jdbc.JdbcTransactionManagerModule;
import com.example.jdbc.service.JdbcService;
import com.example.jdbc.service.ServiceModule;
import com.example.jdbc.service.TransactionService;
import com.example.jdbc.verticle.JdbcVerticle;
import com.example.starter.jdbc.pool.JdbcPoolModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import github.benslabbert.txmanager.PlatformTransactionManager;
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
      ServiceModule.class,
      TransactionManagerModule.class,
      PreparedStatementDslContextModule.class,
      StaticSqlDslContextModule.class,
      JdbcPoolModule.class,
      Provider.EagerModule.class,
      JdbcTransactionManagerModule.class
    })
public interface Provider {

  final class ApplicationContext {

    private ApplicationContext() {}

    private static Provider provider;

    public static Provider get() {
      if (null == provider) {
        throw new IllegalStateException("Provider not initialized");
      }
      return provider;
    }

    public static void set(Provider instance) {
      if (null != provider) {
        throw new IllegalStateException("Provider already initialized");
      }
      provider = instance;
    }
  }

  @Nullable Void init();

  JdbcVerticle jdbcVerticle();

  JdbcService jdbcService();

  TransactionService transactionService();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder postgresConfig(Config.PostgresConfig postgresConfig);

    Provider build();
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(
        Provider provider,
        JdbcTransactionManager jdbcTransactionManager,
        DataSource dataSource,
        @Named("prepared") DSLContext preparedDslContext,
        @Named("static") DSLContext staticDslContext) {
      // this eagerly builds any parameters specified and returns nothing
      ApplicationContext.set(provider);
      PlatformTransactionManager.setTransactionManager(jdbcTransactionManager);
      return null;
    }
  }
}
