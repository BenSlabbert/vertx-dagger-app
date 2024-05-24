/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static com.example.jdbc.generator.entity.generated.jooq.Sequences.PERSON_ID_SEQ;
import static com.example.jdbc.generator.entity.generated.jooq.tables.Person.PERSON;

import com.example.jdbc.generator.entity.generated.jooq.tables.records.PersonRecord;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcQueryRunner;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcTransactionManager;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcUtils;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.QueryException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.InsertResultStep;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JdbcService {

  private static final Logger log = LoggerFactory.getLogger(JdbcService.class);

  private final JdbcTransactionManager jdbcTransactionManager;
  private final JdbcQueryRunner jdbcQueryRunner;
  private final JdbcUtils jdbcUtils;
  private final DSLContext preparedDslContext;
  private final DSLContext staticDslContext;

  @Inject
  JdbcService(
      JdbcTransactionManager jdbcTransactionManager,
      JdbcUtils jdbcUtils,
      JdbcQueryRunner jdbcQueryRunner,
      @Named("prepared") DSLContext preparedDslContext,
      @Named("static") DSLContext staticDslContext) {
    this.jdbcTransactionManager = jdbcTransactionManager;
    this.jdbcUtils = jdbcUtils;
    this.jdbcQueryRunner = jdbcQueryRunner;
    this.preparedDslContext = preparedDslContext;
    this.staticDslContext = staticDslContext;
  }

  public Stream<Long> stream() {
    return jdbcUtils.streamInTransaction(
        staticDslContext
            .select(PERSON.ID)
            .from(PERSON)
            .orderBy(PERSON.ID)
            .getSQL(ParamType.INLINED),
        rs -> rs.getLong(1));
  }

  public void forEach(LongConsumer consumer) {
    try {
      jdbcTransactionManager.begin();
      jdbcQueryRunner.query(
          staticDslContext.select(PERSON.ID).from(PERSON).getSQL(ParamType.INLINED),
          rs -> {
            while (rs.next()) {
              consumer.accept(rs.getLong(1));
            }
            return null;
          });
      jdbcTransactionManager.commit();
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }

  public void runBatchInsert(int numberOfItems) {
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

    try {
      jdbcTransactionManager.begin();

      List<Long> ids =
          jdbcQueryRunner.insertBatch(
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

      log.info("new Ids: {}", ids);
      jdbcTransactionManager.commit();
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }

  public void runSelect() {
    try {
      jdbcTransactionManager.begin();

      List<Long> personIds =
          jdbcQueryRunner.query(
              staticDslContext.select(PERSON.ID).from(PERSON).getSQL(ParamType.INLINED),
              rs -> {
                List<Long> ids = new ArrayList<>();
                while (rs.next()) {
                  ids.add(rs.getLong(1));
                }
                return ids;
              });

      log.info("personIds: {}", personIds);
      jdbcTransactionManager.commit();
    } catch (Exception e) {
      jdbcTransactionManager.rollback();
      throw new QueryException(e);
    }
  }
}
