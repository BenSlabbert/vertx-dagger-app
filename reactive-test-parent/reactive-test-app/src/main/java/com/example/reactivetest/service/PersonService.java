/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import static java.util.logging.Level.SEVERE;

import com.example.commons.transaction.reactive.TransactionBoundary;
import com.example.reactivetest.config.Events;
import com.example.reactivetest.repository.sql.PersonRepository;
import com.example.reactivetest.repository.sql.projection.PersonProjectionFactory.PersonProjection;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.pgclient.PgPool;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class PersonService extends TransactionBoundary {

  private final Vertx vertx;
  private final PersonRepository personRepository;

  @Inject
  PersonService(PgPool pool, Vertx vertx, PersonRepository personRepository) {
    super(pool);
    this.vertx = vertx;
    this.personRepository = personRepository;
  }

  public Future<PersonProjection> create(String name) {
    return doInTransaction(conn -> personRepository.create(conn, name))
        .onSuccess(
            person -> {
              log.info("created person: " + person);
              vertx
                  .eventBus()
                  .publish(Events.PERSON_CREATED, person, new DeliveryOptions().setLocalOnly(true));
            })
        .onFailure(err -> log.log(SEVERE, "person create Transaction failed", err));
  }

  public Future<List<PersonProjection>> findAll() {
    return doInTransaction(personRepository::findAll)
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }
}
