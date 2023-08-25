package com.example.reactivetest.config;

import com.example.commons.config.Config;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

@Module
public class PgPoolConfig {

  private PgPoolConfig() {}

  @Provides
  static PgPool providesPgPool(Vertx vertx, Config config) {
    PgConnectOptions connectOptions =
        new PgConnectOptions()
            .setPort(config.postgresConfig().port())
            .setHost(config.postgresConfig().host())
            .setDatabase(config.postgresConfig().database())
            .setUser(config.postgresConfig().username())
            .setPassword(config.postgresConfig().password())
            .setCachePreparedStatements(true)
            .setPipeliningLimit(256);

    PoolOptions poolOptions = new PoolOptions().setMaxSize(1);

    return PgPool.pool(vertx, connectOptions, poolOptions);
  }
}
