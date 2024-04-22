/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.scope;

import io.vertx.core.impl.NoStackTraceException;
import java.sql.Connection;
import javax.inject.Inject;
import javax.sql.DataSource;

@TransactionScope
public class TransactionManager implements AutoCloseable {

  private final DataSource dataSource;

  private Connection connection = null;
  private volatile boolean commitCalled = false;

  @Inject
  TransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void startTransaction() {
    if (null != connection) {
      throw new IllegalStateException("Transaction already in progress");
    }

    // after we have a connection, we can give this to jOOQ
    // or whatever is being used to talk to the DB

    // todo: create a code generator which will generate jdbc code to handle projections?

    try {
      connection = dataSource.getConnection();
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    } catch (Exception e) {
      throw new NoStackTraceException(e);
    }
  }

  public void commit() {
    if (null == connection) {
      throw new IllegalStateException("connection not established, cannot commit");
    }

    try {
      connection.commit();
      commitCalled = true;
    } catch (Exception e) {
      throw new NoStackTraceException(e);
    }
  }

  @Override
  public void close() {
    endTransaction();
  }

  private void endTransaction() {
    if (null == connection) {
      throw new IllegalStateException("connection not established, cannot end transaction");
    }

    // client has not called commit, therefore something went wrong and we have to roll back
    if (!commitCalled) {
      rollback();
    }

    try {
      connection.close();
      reset();
    } catch (Exception e) {
      throw new NoStackTraceException(e);
    }
  }

  private void rollback() {
    if (null == connection) {
      throw new IllegalStateException("connection not established, cannot rollback");
    }

    if (commitCalled) {
      throw new IllegalStateException("commit has been called, cannot rollback");
    }

    try {
      connection.rollback();
    } catch (Exception e) {
      throw new NoStackTraceException(e);
    }
  }

  private void reset() {
    connection = null;
    commitCalled = false;
  }
}
