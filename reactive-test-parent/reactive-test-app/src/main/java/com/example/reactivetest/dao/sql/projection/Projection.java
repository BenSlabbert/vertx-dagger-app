package com.example.reactivetest.dao.sql.projection;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public interface Projection<T> {

  String getSql();

  T parse(RowSet<Row> rowSet);
}
