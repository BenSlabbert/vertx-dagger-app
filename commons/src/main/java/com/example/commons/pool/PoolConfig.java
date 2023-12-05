/* Licensed under Apache-2.0 2023. */
package com.example.commons.pool;

import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Module
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
class PoolConfig implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(PoolConfig.class);

  private static Pool pool = null;

  @Provides
  @Singleton
  static Pool providesPool(Vertx vertx, Config.PostgresConfig config) {
    log.info("creating pg pool");
    PgConnectOptions connectOptions =
        new PgConnectOptions()
            .setConnectTimeout(5)
            .setPort(config.port())
            .setHost(config.host())
            .setDatabase(config.database())
            .setUser(config.username())
            .setPassword(config.password())
            .setCachePreparedStatements(true)
            .setPipeliningLimit(256);

    PoolOptions poolOptions =
        new PoolOptions()
            .setConnectionTimeout(10)
            .setConnectionTimeoutUnit(TimeUnit.SECONDS)
            .setMaxSize(1);

    pool =
        PgBuilder.pool()
            .with(poolOptions)
            .withConnectHandler(
                conn -> {
                  log.info("got connection in the ConnectHandler!");
                  conn.close();
                })
            .connectingTo(connectOptions)
            .using(vertx)
            .build();

    return pool;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() {
    System.err.println("closing pg pool");

    if (null == pool) return;

    FutureUtil.blockingExecution(pool.close());
  }
}
