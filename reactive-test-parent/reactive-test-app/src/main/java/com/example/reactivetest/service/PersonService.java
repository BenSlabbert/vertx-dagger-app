package com.example.reactivetest.service;

import static java.util.logging.Level.SEVERE;

import com.example.reactivetest.dao.sql.PersonRepository;
import com.example.reactivetest.dao.sql.projection.PersonProjectionFactory;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class PersonService extends TransactionBoundary {

  private final PersonRepository personRepository;

  @Inject
  PersonService(PgPool pool, PersonRepository personRepository) {
    super(pool);
    this.personRepository = personRepository;
  }

  public Future<PersonProjectionFactory.InsertReturningProjection.PersonProjection> create(
      String name) {
    return doInTransaction(conn -> personRepository.create(conn, name))
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }

  public Future<List<PersonProjectionFactory.FindPersonProjection.PersonProjection>> findAll() {
    return doInTransaction(personRepository::findAll)
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }
}
