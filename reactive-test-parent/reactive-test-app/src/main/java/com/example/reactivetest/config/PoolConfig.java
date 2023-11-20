/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.config;

import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
@Module
public class PoolConfig implements AutoCloseable {

  @Inject
  PoolConfig() {}

  private static Pool pool = null;

  @Provides
  static synchronized Pool providesPool(Vertx vertx, Config config) {
    if (pool != null) return pool;

    log.info("creating pg pool");
    PgConnectOptions connectOptions =
        new PgConnectOptions()
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

    pool = Pool.pool(vertx, connectOptions, poolOptions);
    return pool;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() {
    if (null == pool) return;

    FutureUtil.blockingExecution(pool.close());
  }
}
