/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static com.example.commons.mesage.Headers.SAGA_ID_HEADER;
import static com.example.commons.mesage.Headers.SAGA_ROLLBACK_HEADER;

import com.google.auto.value.AutoValue;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import java.time.Duration;

@AutoValue
abstract class SagaStage {

  private static final Logger log = LoggerFactory.getLogger(SagaStage.class);

  SagaStage() {}

  abstract SagaStageHandler handler();

  abstract String commandAddress();

  abstract EventBus eventBus();

  Future<Message<JsonObject>> sendCommand(String sagaId) {
    log.info("%s: sending command to: %s".formatted(sagaId, commandAddress()));

    return handler()
        .getCommand(sagaId)
        .compose(
            message ->
                eventBus()
                    .request(
                        commandAddress(),
                        message,
                        new DeliveryOptions()
                            .setSendTimeout(Duration.ofSeconds(5L).toMillis())
                            .addHeader(SAGA_ID_HEADER, sagaId)));
  }

  Future<Message<Void>> sendRollbackCommand(String sagaId) {
    log.info("%s: sending rollback command to: %s".formatted(sagaId, commandAddress()));

    return handler()
        .onRollBack(sagaId)
        .compose(
            ignore ->
                eventBus()
                    .request(
                        commandAddress(),
                        null,
                        new DeliveryOptions()
                            .addHeader(SAGA_ID_HEADER, sagaId)
                            .addHeader(SAGA_ROLLBACK_HEADER, Boolean.TRUE.toString())));
  }

  Future<Boolean> handleResult(String sagaId, Message<JsonObject> result) {
    log.info("%s: handle result".formatted(sagaId));
    return handler().handleResult(sagaId, result);
  }

  static Builder builder() {
    return new AutoValue_SagaStage.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder handler(SagaStageHandler handler);

    abstract Builder commandAddress(String commandAddress);

    abstract Builder eventBus(EventBus eventBus);

    abstract SagaStage build();
  }
}
