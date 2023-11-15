/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Module
class PgPoolConfig implements AutoCloseable {

  @Inject
  PgPoolConfig() {}

  private static PgPool pool = null;

  @Provides
  @Singleton
  static PgPool providesPgPool(Vertx vertx, Config config) {
    log.info("creating pg pool");
    PgConnectOptions connectOptions =
        new PgConnectOptions()
            .setConnectTimeout(5)
            .setPort(config.postgresConfig().port())
            .setHost(config.postgresConfig().host())
            .setDatabase(config.postgresConfig().database())
            .setUser(config.postgresConfig().username())
            .setPassword(config.postgresConfig().password())
            .setCachePreparedStatements(true)
            .setPipeliningLimit(256);

    PoolOptions poolOptions =
        new PoolOptions()
            .setConnectionTimeout(10)
            .setConnectionTimeoutUnit(TimeUnit.SECONDS)
            .setMaxSize(1);

    pool = PgPool.pool(vertx, connectOptions, poolOptions);
    return pool;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() {
    if (null == pool) return;

    FutureUtil.blockingExecution(pool.close());
  }
}
