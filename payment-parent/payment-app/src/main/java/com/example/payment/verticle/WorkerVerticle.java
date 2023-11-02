/* Licensed under Apache-2.0 2023. */
package com.example.payment.verticle;

import com.example.payment.service.KafkaConsumerService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class WorkerVerticle extends AbstractVerticle {

  private final DataSource dataSource;
  private final KafkaConsumerService kafkaConsumerService;

  @Override
  public void start(Promise<Void> startPromise) {
    log.info("starting WorkerVerticle");

    checkDbConnection(startPromise);

    Future<Void> checkKafka =
        kafkaConsumerService
            .init()
            .onFailure(err -> log.log(Level.SEVERE, "failed to verify kafka connection", err));

    checkKafka.onFailure(startPromise::fail).onSuccess(ignore -> startPromise.complete());
  }

  private void checkDbConnection(Promise<Void> startPromise) {
    try (var ignore = dataSource.getConnection()) {
      log.info("connected to database");
    } catch (Exception e) {
      log.log(Level.SEVERE, "failed to get DB connection", e);
      startPromise.fail(e);
    }
  }

  @Override
  public void stop(Promise<Void> stopPromise) {
    log.warning("stopping");
    stopPromise.complete();
  }
}
