/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.ioc;

import com.example.commons.config.Config;
import com.example.commons.jooq.PreparedStatementDslContextModule;
import com.example.commons.jooq.StaticSqlDslContextModule;
import com.example.commons.transaction.blocking.TransactionManagerModule;
import com.example.jdbc.service.JdbcService;
import com.example.jdbc.service.ServiceModule;
import com.example.jdbc.verticle.JdbcVerticle;
import com.example.starter.jdbc.pool.JdbcPoolModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
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
      Provider.EagerModule.class
    })
public interface Provider {

  final class DI {

    private DI() {}

    private static Provider dagger;
    private static volatile boolean initialized;

    public static Provider get() {
      if (!initialized) {
        throw new IllegalStateException("Component not initialized");
      }
      return dagger;
    }

    public static void set(Provider instance) {
      if (initialized) {
        throw new IllegalStateException("Component already initialized");
      }
      initialized = true;
      dagger = instance;
    }
  }

  @Nullable Void init();

  JdbcVerticle jdbcVerticle();

  JdbcService jdbcService();

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
        DataSource dataSource,
        @Named("prepared") DSLContext preparedDslContext,
        @Named("static") DSLContext staticDslContext) {
      // this eagerly builds any parameters specified and returns nothing
      DI.set(provider);
      return null;
    }
  }
}
