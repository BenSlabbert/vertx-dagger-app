/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope;

import io.vertx.core.impl.NoStackTraceException;
import java.sql.Connection;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TransactionScope
public class TransactionManager implements DslProvider, AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

  private final DataSource dataSource;

  private Connection connection = null;
  private DSLContext dslContext = null;
  private boolean commitCalled = false;

  @Inject
  TransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void startTransaction() {
    if (null != connection) {
      throw new IllegalStateException("Transaction already in progress");
    }

    try {
      connection = dataSource.getConnection();
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
  public synchronized DSLContext getContext() {
    log.info("creating dsl context");
    Settings settings = new Settings().withFetchSize(128);

    if (null != dslContext) {
      return dslContext;
    }

    dslContext = DSL.using(connection, SQLDialect.POSTGRES, settings);
    return dslContext;
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
    dslContext = null;
    commitCalled = false;
  }
}
