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
    System.err.println("calling next");
    boolean hasNext = iterator.hasNext();

    if (!hasNext) {
      System.err.println("done");
      // we are done with success
      return Future.succeededFuture();
    }

    System.err.println("more");
    SagaStage stage = iterator.next();
    Promise<Boolean> promise = Promise.promise();

    return stage
        .sendCommand(promise, sagaId)
        .compose(
            ignore -> {
              //              stage.waitForResult(promise, sagaId);

              // todo: need a timeout for this promise
              return promise.future();
            })
        .recover(
            throwable -> {
              log.warning("%s failed to execute saga".formatted(sagaId));
              return previous().map(ignore -> FALSE);
            })
        .compose(
            success -> {
              System.err.println("got result: " + success);
              return TRUE.equals(success) ? next() : previous();
            });
  }

  private Future<Void> previous() {
    System.err.println("calling next");

    boolean hasPrevious = iterator.hasPrevious();

    if (!hasPrevious) {
      System.err.println("done");
      // we are done with an error
      return Future.failedFuture(new NoStackTraceException("%s: saga failed".formatted(sagaId)));
    }

    System.err.println("more");
    SagaStage stage = iterator.previous();

    // todo: need a way to handle this future failing
    //  maybe a recover, or onFailure
    return stage.sendRollbackCommand(sagaId).compose(v -> previous());
  }
}
