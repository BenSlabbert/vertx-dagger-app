/* Licensed under Apache-2.0 2023. */
package com.example.commons.transaction.reactive;

import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import java.util.function.Function;

public abstract class TransactionBoundary {

  private final Pool pool;

  protected TransactionBoundary(Pool pool) {
    this.pool = pool;
  }

  protected <T> Future<T> doInTransaction(Function<SqlClient, Future<T>> function) {
    return pool.getConnection()
        .compose(
            conn ->
                conn.begin()
                    .compose(tx -> function.apply(conn).compose(res -> tx.commit().map(res)))
                    .map(res -> res)
                    .eventually(() -> conn.close()));
  }
}
