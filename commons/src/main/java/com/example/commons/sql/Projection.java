/* Licensed under Apache-2.0 2023. */
package com.example.commons.sql;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public interface Projection<T> {

  String getSql();

  T parse(RowSet<Row> rowSet);
}
