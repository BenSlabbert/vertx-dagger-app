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
                        projection ->
                            kafkaOutboxService
                                .insert(conn, KafkaMessageFactory.create(projection))
                                .map(outbox -> new Tuple<>(projection, outbox))))
        .onSuccess(
            tuple -> {
              eventService.publishKafkaOutboxEvent(tuple.r().id());
              eventService.publishPersonCreatedEvent(tuple.l());
              log.info("created person: " + tuple.l());
            })
        .onFailure(err -> log.log(SEVERE, "person create Transaction failed", err))
        .map(Tuple::l);
  }

  public Future<List<PersonProjectionFactory.FindPersonProjection.PersonProjection>> findAll() {
    return doInTransaction(personRepository::findAll)
        .onSuccess(values -> log.info("values: " + values))
        .onFailure(err -> log.log(SEVERE, "Transaction failed", err));
  }
}
