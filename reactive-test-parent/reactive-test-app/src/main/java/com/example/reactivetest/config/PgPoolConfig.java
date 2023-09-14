/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.config;

import com.example.commons.config.Config;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.extern.java.Log;

@Log
@Module
public class PgPoolConfig implements AutoCloseable {

  @Inject
  PgPoolConfig() {}

  private static PgPool pool = null;

  @Provides
  static synchronized PgPool providesPgPool(Vertx vertx, Config config) {
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

    pool = PgPool.pool(vertx, connectOptions, poolOptions);
    return pool;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() throws InterruptedException {
    if (null == pool) return;

    CountDownLatch latch = new CountDownLatch(1);
    System.err.println("closing pg pool");
    pool.close()
        .onComplete(
            r -> {
              if (r.failed()) {
                System.err.println("closing pg pool failed: " + r.cause());
              }
              latch.countDown();
            });

    latch.await();
  }
}
