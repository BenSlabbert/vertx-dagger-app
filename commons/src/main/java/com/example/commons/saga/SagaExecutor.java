/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceException;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SagaExecutor {

  private static final Logger log = LoggerFactory.getLogger(SagaExecutor.class);

  private final String sagaId;
  private final ListIterator<SagaStage> iterator;

  SagaExecutor(String sagaId, List<SagaStage> stages) {
    this.sagaId = sagaId;
    this.iterator = stages.listIterator();
  }

  public Future<String> execute() {
    log.info("%s: starting saga".formatted(sagaId));
    return next().map(ignore -> sagaId);
  }

  private Future<Void> next() {
    log.info("calling next");
    boolean hasNext = iterator.hasNext();

    if (!hasNext) {
      log.info("done with success");
      // we are done with success
      return Future.succeededFuture();
    }

    log.info("next");
    SagaStage stage = iterator.next();

    return stage
        .sendCommand(sagaId)
        .compose(msg -> stage.handleResult(sagaId, msg))
        .recover(
            throwable -> {
              log.error("%s failed to execute saga".formatted(sagaId), throwable);
              return previous().map(ignore -> FALSE);
            })
        .compose(
            success -> {
              log.info("got result: " + success);
              return TRUE.equals(success) ? next() : previous();
            });
  }

  private Future<Void> previous() {
    log.info("calling previous");

    boolean hasPrevious = iterator.hasPrevious();

    if (!hasPrevious) {
      log.info("done with failure");
      // we are done with an error
      return Future.failedFuture(new FailedSagaException("%s: saga failed".formatted(sagaId)));
    }

    log.info("more previous");
    SagaStage stage = iterator.previous();

    return stage
        .sendRollbackCommand(sagaId)
        .compose(v -> previous())
        .recover(
            throwable -> {
              if (throwable instanceof FailedSagaException) {
                // do nothing, the saga has failed
                return Future.failedFuture(throwable);
              }

              // sendRollbackCommand failed, log the error and keep unwinding
              log.error("%s failed to execute saga rollback".formatted(sagaId), throwable);
              return previous();
            });
  }

  @SuppressWarnings("java:S110")
  private static class FailedSagaException extends NoStackTraceException {

    public FailedSagaException(String message) {
      super(message);
    }
  }
}
