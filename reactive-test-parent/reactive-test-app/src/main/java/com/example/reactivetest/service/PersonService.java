package com.example.reactivetest.service;

import static java.util.logging.Level.SEVERE;

import com.example.commons.util.Tuple;
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

  private final EventService eventService;
  private final PersonRepository personRepository;
  private final KafkaOutboxService kafkaOutboxService;

  @Inject
  PersonService(
      PgPool pool,
      EventService eventService,
      PersonRepository personRepository,
      KafkaOutboxService kafkaOutboxService) {
    super(pool);
    this.eventService = eventService;
    this.personRepository = personRepository;
    this.kafkaOutboxService = kafkaOutboxService;
  }

  public Future<PersonProjectionFactory.InsertReturningProjection.PersonProjection> create(
      String name) {
    return doInTransaction(
            conn ->
                personRepository
                    .create(conn, name)
                    .compose(
                        p ->
                            kafkaOutboxService
                                .insert(conn, KafkaMessageFactory.create(p))
                                .map(outbox -> new Tuple<>(p, outbox))))
        .onSuccess(
            tuple -> {
              eventService.publishKafkaOutboxEvent(tuple.r().id());
              log.info("created person: " + tuple.l());
            })
        .onFailure(err -> log.log(SEVERE, "person create Transaction failed", err))
        .map(Tuple::r);
  }

  public Future<List<PersonProjectionFactory.FindPersonProjection.PersonProjection>> findAll() {
    return doInTransaction(personRepository::findAll)
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }
}
