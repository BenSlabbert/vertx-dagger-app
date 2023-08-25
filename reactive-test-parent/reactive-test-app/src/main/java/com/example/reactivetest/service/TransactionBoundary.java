package com.example.reactivetest.service;

import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import java.util.function.Function;

abstract class TransactionBoundary {
  private final PgPool pool;

  TransactionBoundary(PgPool pool) {
    this.pool = pool;
  }

  <T> Future<T> doInTransaction(Function<SqlClient, Future<T>> function) {
    return pool.getConnection()
        .compose(
            conn ->
                conn.begin()
                    .compose(tx -> function.apply(conn).compose(res -> tx.commit().map(res)))
                    .map(res -> res)
                    .eventually(v -> conn.close()));
  }
}
