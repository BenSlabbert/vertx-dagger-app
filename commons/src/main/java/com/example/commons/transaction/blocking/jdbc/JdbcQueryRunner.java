/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import java.sql.SQLException;
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

  public <T> T insert(String sql, ResultSetHandler<T> rsh) throws SQLException {
    return queryRunner.insert(jdbcTransactionManager.getConnection(), sql, rsh);
  }

  public int[] batch(String sql, Object[][] params) throws SQLException {
    return queryRunner.batch(jdbcTransactionManager.getConnection(), sql, params);
  }

  public int execute(String sql, Object... params) throws SQLException {
    return queryRunner.execute(jdbcTransactionManager.getConnection(), sql, params);
  }

  public <T> List<T> execute(String sql, ResultSetHandler<T> rsh, Object... params)
      throws SQLException {
    return queryRunner.execute(jdbcTransactionManager.getConnection(), sql, rsh, params);
  }

  public <T> T insert(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
    return queryRunner.insert(jdbcTransactionManager.getConnection(), sql, rsh, params);
  }

  public <T> T insertBatch(String sql, ResultSetHandler<T> rsh, Object[][] params)
      throws SQLException {
    return queryRunner.insertBatch(jdbcTransactionManager.getConnection(), sql, rsh, params);
  }

  public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
    return queryRunner.query(jdbcTransactionManager.getConnection(), sql, rsh);
  }

  public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
    return queryRunner.query(jdbcTransactionManager.getConnection(), sql, rsh, params);
  }

  public int update(String sql) throws SQLException {
    return queryRunner.update(jdbcTransactionManager.getConnection(), sql);
  }

  public int update(String sql, Object param) throws SQLException {
    return queryRunner.update(jdbcTransactionManager.getConnection(), sql, param);
  }

  public int update(String sql, Object... params) throws SQLException {
    return queryRunner.update(jdbcTransactionManager.getConnection(), sql, params);
  }
}
