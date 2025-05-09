/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static com.example.jdbc.generator.entity.generated.jooq.tables.Person.PERSON;

import github.benslabbert.txmanager.annotation.Transactional;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcUtils;
import github.benslabbert.vertxdaggercommons.transaction.blocking.jdbc.JdbcUtilsFactory;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.dbutils.StatementConfiguration;
import org.jooq.DSLContext;
import org.jooq.conf.ParamType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TransactionService {

  private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

  private final JdbcUtils jdbcUtils;
  private final DSLContext staticDslContext;

  @Inject
  TransactionService(
      JdbcUtilsFactory jdbcUtilsFactory, @Named("static") DSLContext staticDslContext) {
    this.jdbcUtils =
        jdbcUtilsFactory.create(new StatementConfiguration.Builder().fetchSize(10).build());
    this.staticDslContext = staticDslContext;
  }

  public void runInTransaction() {
    log.info("in runInTransaction");

    try (var s =
        jdbcUtils.streamInTransaction(
            staticDslContext
                .select(PERSON.ID)
                .from(PERSON)
                .orderBy(PERSON.ID)
                .getSQL(ParamType.INLINED),
            rs -> rs.getLong(1))) {
      s.forEach(id -> log.info("id: {}", id));
    }
  }

  @Transactional
  public void useCreatedTransaction() {
    log.info("in useCreatedTransaction");

    try (var s =
        jdbcUtils.stream(
            staticDslContext
                .select(PERSON.ID)
                .from(PERSON)
                .orderBy(PERSON.ID)
                .getSQL(ParamType.INLINED),
            rs -> rs.getLong(1))) {
      s.forEach(id -> log.info("id: {}", id));
    }
  }
}
