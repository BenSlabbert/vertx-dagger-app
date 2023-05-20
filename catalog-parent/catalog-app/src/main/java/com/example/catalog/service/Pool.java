package com.example.catalog.service;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

public interface Pool {

  Future<SqlConnection> getConnection();
}
