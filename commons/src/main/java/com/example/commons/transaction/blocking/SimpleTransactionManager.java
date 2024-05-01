/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.apache.commons.dbutils.DbUtils;
import org.jooq.ConnectionProvider;
import org.jooq.TransactionContext;
import org.jooq.TransactionProvider;
import org.jooq.exception.DataAccessException;

/**
 * A simple transaction provider that uses a single connection per transaction. <br>
 * Limitations:
 *
 * <ul>
 *   <li>Does not support nested transactions
 *   <li>Uses blocking code, not suitable in a reactive setup
 * </ul>
 */
public class SimpleTransactionManager implements TransactionProvider, ConnectionProvider {

  private static final Logger log = LoggerFactory.getLogger(SimpleTransactionManager.class);
  private static final ThreadLocal<Connection> connThreadLocal = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> txActiveThreadLocal = new ThreadLocal<>();

  private final DataSource dataSource;

  SimpleTransactionManager(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void begin(TransactionContext transactionContext) throws DataAccessException {
    log.debug("begin transaction");

    if (Boolean.TRUE.equals(txActiveThreadLocal.get()) || null != connThreadLocal.get()) {
      throw new DataAccessException("nested transactions are not supported");
    }

    txActiveThreadLocal.set(true);
  }

  @Override
  public void commit(TransactionContext transactionContext) throws DataAccessException {
    log.debug("commit transaction");

    try (Connection connection = connThreadLocal.get()) {
      if (null == connection) {
        throw new DataAccessException("no connection found");
      }

      connection.commit();
    } catch (Exception e) {
      throw new DataAccessException("exception while committing transaction", e);
    } finally {
      connThreadLocal.remove();
      txActiveThreadLocal.remove();
    }
  }

  @Override
  public void rollback(TransactionContext transactionContext) throws DataAccessException {
    log.debug("rollback transaction");

    try (Connection connection = connThreadLocal.get()) {
      DbUtils.rollback(connection);
    } catch (Exception e) {
      throw new DataAccessException("exception while rolling back transaction", e);
    } finally {
      connThreadLocal.remove();
      txActiveThreadLocal.remove();
    }
  }

  @Override
  public Connection acquire() throws DataAccessException {
    log.debug("acquire connection");

    // reuse existing connection if we have one
    Connection currentConn = connThreadLocal.get();
    if (null != currentConn) {
      return currentConn;
    }

    try {
      Connection connection = dataSource.getConnection();
      connThreadLocal.set(connection);

      if (Boolean.TRUE.equals(txActiveThreadLocal.get())) {
        return connection;
      }

      txActiveThreadLocal.set(false);
      return connection;
    } catch (Exception e) {
      throw new DataAccessException("exception while acquiring connection", e);
    }
  }

  @Override
  public void release(Connection connection) throws DataAccessException {
    log.debug("release connection");
    // this is called after each query execution
    // it is called before commit and rollback
    // if we have an ongoing transaction, we have to wait for the commit or rollback to release the
    // connection
    // if there is no ongoing transaction, we can release the connection immediately

    // if we have an active transaction, do not close the connection
    // this will be done in the commit or rollback methods
    if (Boolean.TRUE.equals(txActiveThreadLocal.get())) {
      return;
    }

    try {
      DbUtils.close(connection);
    } catch (Exception e) {
      throw new DataAccessException("exception while releasing connection", e);
    } finally {
      connThreadLocal.remove();
      txActiveThreadLocal.remove();
    }
  }
}
