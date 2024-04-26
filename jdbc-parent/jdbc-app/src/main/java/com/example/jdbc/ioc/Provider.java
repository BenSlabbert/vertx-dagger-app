/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.ioc;

import com.example.commons.config.Config;
import com.example.commons.jooq.StaticSqlDslContextModule;
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
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
      StaticSqlDslContextModule.class,
      JdbcPoolModule.class,
      Provider.EagerModule.class
    })
public interface Provider {

  @Nullable Void init();

  JdbcVerticle jdbcVerticle();

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
    @Nullable static Void provideEager(DataSource dataSource, DSLContext dslContext) {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
