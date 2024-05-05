/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static com.example.jdbc.generator.entity.generated.jooq.Sequences.PERSON_ID_SEQ;
import static com.example.jdbc.generator.entity.generated.jooq.tables.Person.PERSON;

import com.example.commons.transaction.blocking.SimpleTransactionManager;
import com.example.jdbc.generator.entity.generated.jooq.tables.records.PersonRecord;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongConsumer;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.StatementConfiguration;
import org.jooq.DSLContext;
import org.jooq.InsertResultStep;
import org.jooq.conf.ParamType;

@Singleton
public class JdbcService {

  @FunctionalInterface
  interface DoInTransaction<T> {
    T apply(Connection conn) throws SQLException;
  }

  @FunctionalInterface
  interface RunInTransaction {
    void accept(Connection conn) throws SQLException;
  }

  private static final class QueryException extends RuntimeException {
    QueryException(Throwable cause) {
      super(cause);
    }
  }

  private static final Logger log = LoggerFactory.getLogger(JdbcService.class);

  private final SimpleTransactionManager transactionProvider;
  private final DSLContext preparedDslContext;
  private final DSLContext staticDslContext;

  @Inject
  JdbcService(
      SimpleTransactionManager transactionProvider,
      @Named("prepared") DSLContext preparedDslContext,
      @Named("static") DSLContext staticDslContext) {
    this.transactionProvider = transactionProvider;
    this.preparedDslContext = preparedDslContext;
    this.staticDslContext = staticDslContext;
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

  public void commit() {
    transactionProvider.commit(null);
  }

  public Stream<Long> stream() {
    try {
      Connection connection = transactionProvider.acquire();
      PreparedStatement statement =
          connection.prepareStatement(
              staticDslContext
                  .select(PERSON.ID)
                  .from(PERSON)
                  .orderBy(PERSON.ID)
                  .getSQL(ParamType.INLINED));
      statement.setFetchSize(2);

      Wrapper wrapper = new Wrapper();
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
                    return rs.getLong(1);
                  }

                  return null;
                } catch (SQLException e) {
                  throw new QueryException(e);
                }
              })
          .takeWhile(Objects::nonNull)
          .onClose(wrapper::close);
    } catch (Exception e) {
      transactionProvider.rollback(null);
      throw new QueryException(e);
    }
  }

  public void forEach(LongConsumer consumer) {
    useConnection(
        conn -> {
          new QueryRunner()
              .query(
                  conn,
                  staticDslContext.select(PERSON.ID).from(PERSON).getSQL(ParamType.INLINED),
                  rs -> {
                    while (rs.next()) {
                      consumer.accept(rs.getLong(1));
                    }
                    return null;
                  });
        });
  }

  public void runBatchInsert(int numberOfItems) {
    var ids =
        useConnection(
            conn -> {
              InsertResultStep<PersonRecord> query =
                  preparedDslContext
                      .insertInto(PERSON)
                      .set(PERSON.ID, PERSON_ID_SEQ.nextval())
                      .set(PERSON.NAME, "Alice")
                      .returning(PERSON.ID);

              List<Object> bindValues = query.getBindValues();
              Object[] array = bindValues.toArray();
              Object[][] bindParams = new Object[numberOfItems][bindValues.size()];

              for (int i = 0; i < numberOfItems; i++) {
                bindParams[i] = array;
              }

              return new QueryRunner()
                  .insertBatch(
                      conn,
                      query.getSQL(ParamType.INDEXED),
                      rs -> {
                        List<Long> longs = new ArrayList<>(numberOfItems);
                        while (rs.next()) {
                          long newId = rs.getLong(1);
                          longs.add(newId);
                        }
                        return longs;
                      },
                      bindParams);
            });

    log.info("new Ids: " + ids);
  }

  public void runSelect() {
    useConnection(
        conn -> {
          var statementConfiguration =
              new StatementConfiguration.Builder()
                  .fetchSize(2)
                  .queryTimeout(Duration.ofSeconds(10L))
                  .build();

          // statementConfiguration is ignored here as it only applies to prepared statements
          List<Long> personIds =
              new QueryRunner(statementConfiguration)
                  .query(
                      conn,
                      staticDslContext.select(PERSON.ID).from(PERSON).getSQL(ParamType.INLINED),
                      rs -> {
                        List<Long> ids = new ArrayList<>();
                        while (rs.next()) {
                          ids.add(rs.getLong(1));
                        }
                        return ids;
                      });

          log.info("personIds: " + personIds);
        });
  }

  private <T> T useConnection(DoInTransaction<T> function) {
    Connection conn = null;
    try {
      transactionProvider.begin(null);
      conn = transactionProvider.acquire();
      T res = function.apply(conn);
      transactionProvider.commit(null);
      return res;
    } catch (Exception e) {
      transactionProvider.rollback(null);
      throw new QueryException(e);
    } finally {
      transactionProvider.release(conn);
    }
  }

  private void useConnection(RunInTransaction function) {
    Connection conn = null;
    try {
      transactionProvider.begin(null);
      conn = transactionProvider.acquire();
      function.accept(conn);
      transactionProvider.commit(null);
    } catch (Exception e) {
      transactionProvider.rollback(null);
      throw new QueryException(e);
    } finally {
      transactionProvider.release(conn);
    }
  }
}
