/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import github.benslabbert.txmanager.TransactionManager;
import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JdbcTransactionManager implements TransactionManager {

  private static final Logger log = LoggerFactory.getLogger(JdbcTransactionManager.class);
  private static final ThreadLocal<Deque<Connection>> threadLocalDeque = new ThreadLocal<>();

  private final AtomicBoolean closing = new AtomicBoolean(false);
  private final DataSource dataSource;

  @Inject
  JdbcTransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Connection getConnection() {
    Deque<Connection> deque = threadLocalDeque.get();
    if (null == deque) {
      throw new IllegalStateException("cannot get connection: no transaction started");
    }

    Connection connection = deque.peek();
    if (null == connection) {
      throw new IllegalStateException("cannot get connection: no transaction in progress");
    }

    return connection;
  }

  @Override
  public void begin() {
    if (closing.get()) {
      throw new IllegalStateException("cannot begin transaction: transaction manager is closing");
    }

    Deque<Connection> deque = threadLocalDeque.get();
    if (null == deque) {
      threadLocalDeque.set(new ArrayDeque<>(2));
      deque = threadLocalDeque.get();
    }

    try {
      Connection connection = dataSource.getConnection();
      log.debug("pushing connection to thread local deque");
      deque.push(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    }
  }

  @Override
  public void ensureActive() {
    Deque<Connection> deque = threadLocalDeque.get();
    if (null == deque || deque.isEmpty()) {
      throw new IllegalStateException("no transaction in progress");
    }
  }

  @Override
  public void commit() {
    Deque<Connection> deque = threadLocalDeque.get();
    if (null == deque || deque.isEmpty()) {
      throw new IllegalStateException("cannot commit: no transaction started");
    }

    log.debug("commit: poll connection from thread local deque");
    Connection connection = deque.poll();
    if (null == connection) {
      throw new IllegalStateException("cannot commit: no transaction in progress");
    }

    try {
      DbUtils.commitAndClose(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    } finally {
      if (deque.isEmpty()) {
        log.debug("commit: clearing thread local deque");
        threadLocalDeque.remove();
      }
    }
  }

  @Override
  public void rollback() {
    Deque<Connection> deque = threadLocalDeque.get();
    if (null == deque || deque.isEmpty()) {
      throw new IllegalStateException("cannot rollback: no transaction started");
    }

    log.debug("rollback: poll connection from thread local deque");
    Connection connection = deque.poll();
    if (null == connection) {
      throw new IllegalStateException("cannot rollback: no transaction in progress");
    }

    try {
      DbUtils.rollbackAndClose(connection);
    } catch (Exception e) {
      throw new JdbcTransactionException(e);
    } finally {
      if (deque.isEmpty()) {
        log.debug("rollback: clearing thread local deque");
        threadLocalDeque.remove();
      }
    }
  }

  @Override
  public void close() {
    log.debug("closing transaction manager");
    closing.set(true);
  }
}
