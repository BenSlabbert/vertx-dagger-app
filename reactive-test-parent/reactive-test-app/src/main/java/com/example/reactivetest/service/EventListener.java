package com.example.reactivetest.service;

import com.example.reactivetest.config.EventBusConfig;
import com.example.reactivetest.dao.sql.OutboxRepository;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgPool;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class EventListener extends TransactionBoundary {

  @Inject
  public EventListener(
      Vertx vertx,
      KafkaProducerService kafkaProducerService,
      PgPool pool,
      OutboxRepository outboxRepository) {
    super(pool);

    vertx
        .eventBus()
        .consumer(
            EventBusConfig.KAFKA_OUTBOX_SEND,
            m -> {
              long id = (long) m.body();

              doInTransaction(
                      conn ->
                          outboxRepository
                              .get(conn, id)
                              .map(KafkaMessageFactory::create)
                              .compose(kafkaProducerService::emitPersonCreated)
                            .compose(metadata -> outboxRepository.delete(conn, id).map(ignore -> metadata)))
                  .onSuccess(
                      metadata -> {
                        log.info("metadata.getTopic: " + metadata.getTopic());
                        log.info("metadata.getOffset: " + metadata.getOffset());
                        log.info("metadata.getPartition: " + metadata.getPartition());
                        log.info("metadata.getTimestamp: " + metadata.getTimestamp());
                      })
                  .onFailure(err -> log.log(Level.SEVERE, "failed to send message", err));
            });
  }
}
