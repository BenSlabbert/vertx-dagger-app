package com.example.reactivetest.service;

import static java.util.logging.Level.SEVERE;

import com.example.commons.util.Tuple;
import com.example.reactivetest.config.EventBusConfig;
import com.example.reactivetest.dao.sql.PersonRepository;
import com.example.reactivetest.dao.sql.projection.PersonProjectionFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
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
  private final KafkaOutboxService kafkaOutboxService;
  private final KafkaProducerService kafkaProducerService;

  @Inject
  PersonService(
      Vertx vertx,
      PgPool pool,
      PersonRepository personRepository,
      KafkaOutboxService kafkaOutboxService,
      KafkaProducerService kafkaProducerService) {
    super(pool);
    this.vertx = vertx;
    this.personRepository = personRepository;
    this.kafkaOutboxService = kafkaOutboxService;
    this.kafkaProducerService = kafkaProducerService;
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
              vertx.eventBus().send(EventBusConfig.KAFKA_OUTBOX_SEND, tuple.r().id());
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
