/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import com.example.commons.kafka.consumer.MessageHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.NoStackTraceException;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
public class SagaExecutor {

  private final String sagaId;
  private final ListIterator<SagaStage> iterator;

  @Getter private final List<MessageHandler> messageHandlers;

  SagaExecutor(String sagaId, List<SagaStage> stages) {
    this.sagaId = sagaId;
    this.messageHandlers = stages.stream().map(ss -> (MessageHandler) ss).toList();
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
      log.info("done");
      // we are done with success
      return Future.succeededFuture();
    }

    log.info("more");
    SagaStage stage = iterator.next();
    Promise<Boolean> promise = Promise.promise();

    return stage
        .sendCommandAndAwaitResponse(promise, sagaId)
        .compose(
            ignore -> {
              // TODO: we need a timeout for this
              //  promise.future is retuning io.vertx.core.impl.future.FutureImpl
              //  and this is causing a class cast exception
              return promise.future();
            })
        .recover(
            throwable -> {
              log.log(Level.SEVERE, "%s failed to execute saga".formatted(sagaId), throwable);
              return previous().map(ignore -> FALSE);
            })
        .compose(
            success -> {
              log.info("got result: " + success);
              return TRUE.equals(success) ? next() : previous();
            });
  }

  private Future<Void> previous() {
    log.info("calling next");

    boolean hasPrevious = iterator.hasPrevious();

    if (!hasPrevious) {
      log.info("done");
      // we are done with an error
      return Future.failedFuture(new NoStackTraceException("%s: saga failed".formatted(sagaId)));
    }

    log.info("more");
    SagaStage stage = iterator.previous();

    // todo: need a way to handle this future failing
    //  maybe a recover, or onFailure
    return stage.sendRollbackCommand(sagaId).compose(v -> previous());
  }
}
