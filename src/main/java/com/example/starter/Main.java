package com.example.starter;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import com.example.starter.config.Config;
import com.example.starter.repository.RepositoryModule;
import com.example.starter.service.ServiceModule;
import com.example.starter.verticle.ApiVerticle;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.netty.resolver.dns.DefaultDnsServerAddressStreamProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Module
public class Main {

  private static Config config = Config.defaults();
  private static Vertx vertx;

  public static void main(String[] args) throws IOException {
    log.log(INFO, "starting app: {0}", new Object[] {Arrays.toString(args)});

    parseArgs(args);

    List<String> reachableNameServers = getReachableNameServers();
    log.log(INFO, "reachableNameServers: {0}", new Object[] {reachableNameServers});

    vertx =
        Vertx.vertx(
            new VertxOptions()
                .setPreferNativeTransport(true)
                .setAddressResolverOptions(
                    new AddressResolverOptions().setServers(reachableNameServers)));

    Runtime.getRuntime().addShutdownHook(new Thread(getTerminationRunnable(vertx)));

    Provider dagger = DaggerMain_Provider.create();

    DeploymentOptions deploymentOptions = new DeploymentOptions();
    deploymentOptions.setInstances(2);

    vertx
        .deployVerticle(dagger::provideMainVerticle, deploymentOptions)
        .onFailure(throwable -> log.log(SEVERE, "error while deploying verticle", throwable))
        .onSuccess(id -> log.log(INFO, "deployment id: {0}", new Object[] {id}));
  }

  private static List<String> getReachableNameServers() {
    // not sure if this is still needed
    // root cause might be in the custom jlink runtime
    return DefaultDnsServerAddressStreamProvider.defaultAddressList().stream()
        .filter(
            ns -> {
              try {
                return ns.getAddress().isReachable(1000);
              } catch (IOException e) {
                // do nothing
              }
              return false;
            })
        .map(ns -> ns.getAddress().getHostAddress())
        .toList();
  }

  private static void parseArgs(String[] args) throws IOException {
    if (args.length == 0) return;

    List<String> parsed =
        Arrays.stream(args).filter(s -> !s.startsWith("-X") && !s.startsWith("-D")).toList();

    if (parsed.isEmpty()) return;

    if (parsed.size() != 1) {
      log.info("invalid config, only provide 1 path to a config file");
      System.exit(1);
    }

    log.log(INFO, "parsing config from: {0}", new Object[] {parsed});
    String s = Files.readString(Paths.get(parsed.get(0)));
    config = Config.fromJson((JsonObject) Json.decodeValue(s));
  }

  private static Runnable getTerminationRunnable(Vertx vertx) {
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
  @Component(modules = {RepositoryModule.class, ServiceModule.class, Main.class})
  interface Provider {
    ApiVerticle provideMainVerticle();
  }

  @Provides
  static Vertx providesVertx() {
    return vertx;
  }

  @Provides
  static Config providesConfig() {
    return config;
  }

  @Provides
  static Config.HttpConfig providesHttpConfig() {
    return config.httpConfig();
  }

  @Provides
  static Config.RedisConfig providesRedisConfig() {
    return config.redisConfig();
  }
}
