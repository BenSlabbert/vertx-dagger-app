package com.example.commons.transaction.blocking.jdbc;

import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;

public class JdbcTransactionManager {

  private static final ThreadLocal<Connection> connThreadLocal = new ThreadLocal<>();

  private final DataSource dataSource;

  JdbcTransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void begin() {
    if (null != connThreadLocal.get()) {
      throw new IllegalStateException("transaction already started");
    }

    try {
      Connection connection = dataSource.getConnection();
      connThreadLocal.set(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    }
  }

  public void commit() {
    Connection connection = connThreadLocal.get();
    if (null == connection) {
      throw new IllegalStateException("no transaction in progress");
    }

    try {
      DbUtils.commitAndClose(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    } finally {
      connThreadLocal.remove();
    }
  }

  public void rollback() {
    Connection connection = connThreadLocal.get();
    if (null == connection) {
      throw new IllegalStateException("no transaction in progress");
    }

    try {
      DbUtils.rollbackAndClose(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    } finally {
      connThreadLocal.remove();
    }
  }
}
