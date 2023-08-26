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
  private final KafkaProducerService kafkaProducerService;

  @Inject
  PersonService(
      PgPool pool, PersonRepository personRepository, KafkaProducerService kafkaProducerService) {
    super(pool);
    this.personRepository = personRepository;
    this.kafkaProducerService = kafkaProducerService;
  }

  public Future<PersonProjectionFactory.InsertReturningProjection.PersonProjection> create(
      String name) {
    return doInTransaction(conn -> personRepository.create(conn, name))
        .onSuccess(
            values -> {
              log.info("values: " + values);
              kafkaProducerService.write();
            })
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }

  public Future<List<PersonProjectionFactory.FindPersonProjection.PersonProjection>> findAll() {
    return doInTransaction(personRepository::findAll)
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }
}
