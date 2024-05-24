/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import com.example.reactivetest.config.Events;
import com.example.reactivetest.repository.sql.PersonRepository;
import com.example.reactivetest.repository.sql.projection.PersonProjectionFactory.PersonProjection;
import github.benslabbert.vertxdaggercommons.transaction.reactive.TransactionBoundary;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.sqlclient.Pool;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PersonService extends TransactionBoundary {

  private static final Logger log = LoggerFactory.getLogger(PersonService.class);

  private final Vertx vertx;
  private final PersonRepository personRepository;

  @Inject
  PersonService(Pool pool, Vertx vertx, PersonRepository personRepository) {
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
        .onFailure(err -> log.error("person create Transaction failed", err));
  }

  public Future<List<PersonProjection>> findAll() {
    return doInTransaction(personRepository::findAll)
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.error("Transaction failed", err));
  }
}
