/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.SqlClient;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StartupService extends TransactionBoundary {

  private final KafkaOutboxService kafkaOutboxService;
  private final KafkaProducerService kafkaProducerService;

  @Inject
  StartupService(
      PgPool pool,
      KafkaOutboxService kafkaOutboxService,
      KafkaProducerService kafkaProducerService) {
    super(pool);
    this.kafkaOutboxService = kafkaOutboxService;
    this.kafkaProducerService = kafkaProducerService;
  }

  public Future<Void> run() {
    return doInTransaction(
            conn ->
                kafkaOutboxService
                    .next(conn)
                    .compose(
                        optional -> {
                          if (optional.isEmpty()) return Future.succeededFuture(true);

                          return process(conn, optional.get()).map(ignore -> false);
                        }))
        // outside of transaction
        .compose(
            complete -> {
              if (Boolean.TRUE.equals(complete)) {
                return Future.succeededFuture();
              }

              return run();
            });
  }

  private Future<OutboxProjectionFactory.DeleteFromOutbox.DeleteOutboxProjection> process(
      SqlClient conn, OutboxProjectionFactory.GetFromOutboxProjection projection) {
    return kafkaProducerService
        .emitPersonCreated(KafkaMessageFactory.create(projection))
        .compose(metadata -> kafkaOutboxService.remove(conn, projection.id()));
  }
}
