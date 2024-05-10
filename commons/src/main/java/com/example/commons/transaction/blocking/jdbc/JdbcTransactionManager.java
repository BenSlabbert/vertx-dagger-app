/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import github.benslabbert.txmanager.TransactionManager;
import java.sql.Connection;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;

@Singleton
public class JdbcTransactionManager implements TransactionManager {

  private static final ThreadLocal<Connection> connThreadLocal = new ThreadLocal<>();

  private final DataSource dataSource;

  @Inject
  JdbcTransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Connection getConnection() {
    Connection connection = connThreadLocal.get();
    if (null == connection) {
      throw new IllegalStateException("cannot get connection: no transaction in progress");
    }
    return connection;
  }

  @Override
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

  @Override
  public void commit() {
    Connection connection = connThreadLocal.get();
    if (null == connection) {
      throw new IllegalStateException("cannot commit: no transaction in progress");
    }

    try {
      DbUtils.commitAndClose(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    } finally {
      connThreadLocal.remove();
    }
  }

  @Override
  public void rollback() {
    Connection connection = connThreadLocal.get();
    if (null == connection) {
      throw new IllegalStateException("cannot rollback: no transaction in progress");
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
