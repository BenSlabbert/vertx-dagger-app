/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.ioc;

import com.example.commons.closer.CloserModule;
import com.example.commons.config.Config;
import com.example.commons.jooq.StaticSqlDslContextModule;
import com.example.starter.reactive.pool.PoolModule;
import com.example.warehouse.repository.RepositoryModule;
import com.example.warehouse.service.ServiceModule;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      StaticSqlDslContextModule.class,
      CloserModule.class,
      PoolModule.class,
      ServiceModule.class,
      RepositoryModule.class,
      Provider.EagerModule.class
    })
public interface TestProvider extends Provider {

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

    @BindsInstance
    Builder authenticationProvider(AuthenticationProvider authenticationProvider);

    TestProvider build();
  }
}
