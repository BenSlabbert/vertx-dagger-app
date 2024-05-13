/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

@Singleton
public class JdbcQueryRunner {

  private final JdbcTransactionManager jdbcTransactionManager;
  private final QueryRunner queryRunner;

  @Inject
  JdbcQueryRunner(JdbcTransactionManager jdbcTransactionManager) {
    this.queryRunner = new QueryRunner();
    this.jdbcTransactionManager = jdbcTransactionManager;
  }

  public <T> T insert(String sql, ResultSetHandler<T> rsh) {
    try {
      return queryRunner.insert(jdbcTransactionManager.getConnection(), sql, rsh);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public int[] batch(String sql, Object[][] params) {
    try {
      return queryRunner.batch(jdbcTransactionManager.getConnection(), sql, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public int execute(String sql, Object... params) {
    try {
      return queryRunner.execute(jdbcTransactionManager.getConnection(), sql, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params) {
    try {
      return queryRunner.execute(jdbcTransactionManager.getConnection(), sql, rsh, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) {
    try {
      return queryRunner.insert(jdbcTransactionManager.getConnection(), sql, rsh, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params) {
    try {
      return queryRunner.insertBatch(jdbcTransactionManager.getConnection(), sql, rsh, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public <T> T query(String sql, ResultSetHandler<T> rsh) {
    try {
      return queryRunner.query(jdbcTransactionManager.getConnection(), sql, rsh);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) {
    try {
      return queryRunner.query(jdbcTransactionManager.getConnection(), sql, rsh, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public int update(String sql) {
    try {
      return queryRunner.update(jdbcTransactionManager.getConnection(), sql);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public int update(String sql, Object param) {
    try {
      return queryRunner.update(jdbcTransactionManager.getConnection(), sql, param);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  public int update(String sql, Object... params) {
    try {
      return queryRunner.update(jdbcTransactionManager.getConnection(), sql, params);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }
}
