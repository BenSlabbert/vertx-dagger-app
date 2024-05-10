/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.dbutils.DbUtils;

@Singleton
public class JdbcUtils {

  private final JdbcTransactionManager jdbcTransactionManager;

  @Inject
  JdbcUtils(JdbcTransactionManager jdbcTransactionManager) {
    this.jdbcTransactionManager = jdbcTransactionManager;
  }

  @FunctionalInterface
  public interface DoInTransaction<T> {
    T apply(Connection conn) throws SQLException;
  }

  @FunctionalInterface
  public interface RunInTransaction {
    void accept(Connection conn) throws SQLException;
  }

  @FunctionalInterface
  public interface UseTransaction<T> {
    T apply(Connection conn) throws SQLException;
  }

  private static class Wrapper {
    PreparedStatement statement = null;
    ResultSet resultSet = null;

    private void wrap(PreparedStatement statement) {
      this.statement = statement;
    }

    private void wrap(ResultSet resultSet) {
      this.resultSet = resultSet;
    }

    private void close() {
      DbUtils.closeQuietly(resultSet);
      DbUtils.closeQuietly(statement);
    }
  }

  /** Stream the results of a query in a dedicated transaction. */
  public <T> Stream<T> streamInTransaction(String sql, Function<ResultSet, T> mapper) {
    try {
      jdbcTransactionManager.begin();
      return stream(sql, mapper).onClose(jdbcTransactionManager::commit);
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }

  /** Stream the results of a query in an existing transaction. */
  public <T> Stream<T> stream(String sql, Function<ResultSet, T> mapper) {
    Wrapper wrapper = new Wrapper();
    try {
      Connection connection = jdbcTransactionManager.getConnection();
      PreparedStatement statement = connection.prepareStatement(sql);
      wrapper.wrap(statement);

      ResultSet rs = statement.executeQuery();
      wrapper.wrap(rs);

      return Stream.generate(
              () -> {
                try {
                  if (rs.isClosed()) {
                    return null;
                  }

                  if (rs.next()) {
                    return mapper.apply(rs);
                  }

                  return null;
                } catch (SQLException e) {
                  throw new QueryException(e);
                }
              })
          .takeWhile(Objects::nonNull)
          .onClose(wrapper::close);
    } catch (Exception e) {
      wrapper.close();
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }

  /** use an existing transaction */
  public <T> T useTransaction(UseTransaction<T> function) {
    try {
      Connection conn = jdbcTransactionManager.getConnection();
      return function.apply(conn);
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }

  /** execute in dedicated transaction */
  public <T> T doInTransaction(DoInTransaction<T> function) {
    try {
      jdbcTransactionManager.begin();
      Connection conn = jdbcTransactionManager.getConnection();
      T res = function.apply(conn);
      jdbcTransactionManager.commit();
      return res;
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }

  /** execute in dedicated transaction */
  public void runInTransaction(RunInTransaction function) {
    try {
      jdbcTransactionManager.begin();
      Connection conn = jdbcTransactionManager.getConnection();
      function.accept(conn);
      jdbcTransactionManager.commit();
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }
}
