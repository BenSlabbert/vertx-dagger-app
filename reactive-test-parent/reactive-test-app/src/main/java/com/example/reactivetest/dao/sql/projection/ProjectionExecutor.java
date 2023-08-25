package com.example.reactivetest.dao.sql.projection;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public class ProjectionExecutor {

  private ProjectionExecutor() {}

  public static <T> Future<T> execute(SqlClient conn, Projection<T> projection) {
    return conn.query(projection.getSql()).execute().map(projection::parse);
  }
}
