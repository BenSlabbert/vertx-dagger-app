/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.repository.sql.projection;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public class ProjectionExecutor {

  private ProjectionExecutor() {}

  public static <T> Future<T> execute(SqlClient conn, Projection<T> projection) {
    return conn.query(projection.getSql()).execute().map(projection::parse);
  }
}
