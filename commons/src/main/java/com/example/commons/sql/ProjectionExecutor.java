/* Licensed under Apache-2.0 2023. */
package com.example.commons.sql;

import static org.jooq.conf.ParamType.INLINED;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;

public class ProjectionExecutor {

  private ProjectionExecutor() {}

  public static <T> Future<T> execute(SqlClient conn, Projection<T> projection) {
    // todo add config option to show logging
    String sql = projection.getSql().getSQL(INLINED);
    return conn.query(sql).execute().map(projection::parse);
  }
}
