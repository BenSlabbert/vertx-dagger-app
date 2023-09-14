/* Licensed under Apache-2.0 2023. */
package com.example.commons.shutdown.hooks;

import io.vertx.core.Vertx;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ShutdownHookProvider {

  private ShutdownHookProvider() {}

  public static Thread get(Vertx vertx, Iterable<AutoCloseable> closeables) {
    return new Thread(
        () -> {
          // cannot use logger here
          System.err.println("running shutdown hook");

          System.err.println("closing created resources...");
          for (AutoCloseable service : closeables) {
            try {
              service.close();
            } catch (Exception e) {
              System.err.println("unable to close resources: " + e);
            }
          }

          System.err.println("closing created resources...done");

          if (null == vertx) return;

          CountDownLatch latch = new CountDownLatch(1);
          vertx.close(
              ar -> {
                if (ar.failed()) {
                  System.err.println("failed to shutdown");
                } else {
                  System.err.println("shut down");
                }
                latch.countDown();
              });

          System.err.println("waiting 2mins for shutdown");
          try {
            if (!latch.await(2, TimeUnit.MINUTES)) {
              System.err.println("Timed out waiting to undeploy all");
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
          }
        });
  }
}
