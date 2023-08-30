package com.example.reactivetest.service;

import io.vertx.pgclient.PgPool;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class KafkaOutboxEventListener extends TransactionBoundary {

  @Inject
  KafkaOutboxEventListener(
      PgPool pool,
      EventService eventService,
      KafkaOutboxService kafkaOutboxService,
      KafkaProducerService kafkaProducerService) {
    super(pool);

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
                                    kafkaOutboxService.remove(conn, id).map(ignore -> metadata)))
                .onSuccess(
                    metadata -> {
                      log.info("metadata.getTopic: " + metadata.getTopic());
                      log.info("metadata.getOffset: " + metadata.getOffset());
                      log.info("metadata.getPartition: " + metadata.getPartition());
                      log.info("metadata.getTimestamp: " + metadata.getTimestamp());
                    })
                .onFailure(err -> log.log(Level.SEVERE, "failed to send message", err)));
  }
}
