package com.example.starter;

import com.example.starter.repository.UserRepositoryModule;
import com.example.starter.service.UserServiceModule;
import com.example.starter.verticle.ApiVerticle;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Module
public class Main {

  private static Vertx vertx;

  public static void main(String[] args) {
    log.info("starting app");

    vertx =
        Vertx.vertx(
            new VertxOptions()
                .setPreferNativeTransport(true)
                .setAddressResolverOptions(
                    new AddressResolverOptions().addServer("1.1.1.1").addServer("8.8.8.8")));

    Runtime.getRuntime().addShutdownHook(new Thread(getTerminationRunnable(vertx)));

    Provider dagger = DaggerMain_Provider.create();

    DeploymentOptions deploymentOptions = new DeploymentOptions();
    deploymentOptions.setInstances(2);
    vertx
        .deployVerticle(dagger::provideMainVerticle, deploymentOptions)
        .onFailure(throwable -> log.info("deployment id: " + throwable.toString()))
        .onSuccess(id -> log.info("deployment id: " + id));
  }

  public static Runnable getTerminationRunnable(Vertx vertx) {
    return () -> {
      // cannot use logger here
      System.err.println("running shutdown hook");

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
    };
  }

  @Singleton
  @Component(modules = {UserRepositoryModule.class, UserServiceModule.class, Main.class})
  interface Provider {
    ApiVerticle provideMainVerticle();
  }

  @Provides
  static Vertx providesVertx() {
    return vertx;
  }
}
