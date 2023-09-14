/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.pgclient.PgPool;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class KafkaOutboxEventListener extends TransactionBoundary implements AutoCloseable {

  private final MessageConsumer<Long> consumer;

  @Inject
  KafkaOutboxEventListener(
      PgPool pool,
      EventService eventService,
      KafkaOutboxService kafkaOutboxService,
      KafkaProducerService kafkaProducerService) {
    super(pool);

    this.consumer =
        eventService.consumeKafkaOutboxEvent(
            id ->
                doInTransaction(
                        conn ->
                            kafkaOutboxService
                                .get(conn, id)
                                .map(KafkaMessageFactory::create)
                                .compose(kafkaProducerService::emitPersonCreated)
                                .compose(
                                    metadata ->
                                        kafkaOutboxService
                                            .remove(conn, id)
                                            .map(ignore -> metadata)))
                    .onSuccess(metadata -> log.info("written to kafka: " + metadata))
                    .onFailure(err -> log.log(Level.SEVERE, "failed to send message", err)));
  }

  @Override
  public void close() {
    consumer.unregister();
  }
}
