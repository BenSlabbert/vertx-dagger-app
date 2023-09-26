/* Licensed under Apache-2.0 2023. */
package com.example.commons.sql;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.jooq.AttachableQueryPart;

public interface Projection<T> {

  AttachableQueryPart getSql();

  T parse(RowSet<Row> rowSet);
}
