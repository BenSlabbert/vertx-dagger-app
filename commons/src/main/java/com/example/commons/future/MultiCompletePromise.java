/* Licensed under Apache-2.0 2023. */
package com.example.commons.future;

import io.vertx.core.Promise;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * useful class for when you have a single promise that is completed from different processes
 * concurrently <br>
 * only the Nth call to complete will actually complete the promise
 */
public final class MultiCompletePromise {

  private final Promise<Void> promise;
  private final AtomicInteger counter;

  public static MultiCompletePromise create(Promise<Void> promise, int times) {
    return new MultiCompletePromise(promise, times);
  }

  private MultiCompletePromise(Promise<Void> promise, int times) {
    this.promise = promise;
    this.counter = new AtomicInteger(times);
  }

  public void complete() {
    if (counter.decrementAndGet() == 0) {
      promise.complete();
    }
  }
}
