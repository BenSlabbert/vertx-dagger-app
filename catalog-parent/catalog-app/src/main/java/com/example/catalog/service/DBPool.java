package com.example.catalog.service;

import com.example.commons.config.Config;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class DBPool implements Pool, AutoCloseable {

  private final Vertx vertx;
  private final PgPool pool;

  // todo: maybe replace postgresql with redis as storage to avoid this native-image issue
  @Inject
  DBPool(Vertx vertx, Config.PostgresConfig postgresConfig) {
    PgConnectOptions connectOptions =
        new PgConnectOptions()
            .setPort(postgresConfig.port())
            .setHost(postgresConfig.host())
            .setDatabase(postgresConfig.database())
            .setUser(postgresConfig.username())
            .setPassword(postgresConfig.password())
            .setReconnectAttempts(5)
            .setReconnectInterval(1000);

    PoolOptions poolOptions = new PoolOptions().setMaxSize(2);

    this.pool = PgPool.pool(vertx, connectOptions, poolOptions);
    this.vertx = vertx;
  }

  public Future<SqlConnection> getConnection() {
    return pool.getConnection();
  }

  @Override
  public void close() {
    vertx
        .executeBlocking(t -> pool.close())
        .onFailure(err -> log.log(Level.SEVERE, "failed to close db pool", err))
        .onSuccess(o -> log.info("db pool closed"));
  }
}
