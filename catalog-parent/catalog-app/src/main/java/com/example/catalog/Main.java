/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import com.example.catalog.ioc.DaggerProvider;
import com.example.catalog.ioc.Provider;
import com.example.commons.config.Config;
import com.example.commons.config.ParseConfig;
import com.example.commons.networking.ReachableNameServers;
import com.example.commons.shutdown.hooks.ShutdownHookProvider;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.dns.AddressResolverOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Module
public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  private static Config config;
  private static Vertx vertx;

  static {
    System.setProperty("org.jooq.no-tips", "true");
    System.setProperty("org.jooq.no-logo", "true");
  }

  public static void main(String[] args) {
    log.info("starting app: " + Arrays.toString(args));

    // breaks on native image
    // https://github.com/oracle/graal/issues/5510
    LocalDateTime parse =
        LocalDateTime.parse(
            "4714-11-24 00:00:00 BC",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss G", Locale.ROOT));
    log.info("parse: " + parse);

    config = ParseConfig.parseArgs(args);

    Objects.requireNonNull(config.postgresConfig());
    Objects.requireNonNull(config.httpConfig());
    Objects.requireNonNull(config.redisConfig());
    Objects.requireNonNull(config.verticleConfig());
    Objects.requireNonNull(config.serviceRegistryConfig());

    List<String> reachableNameServers = ReachableNameServers.getReachableNameServers();
    log.info("reachableNameServers: " + reachableNameServers);

    vertx =
        Vertx.vertx(
            new VertxOptions()
                .setInternalBlockingPoolSize(1)
                .setWorkerPoolSize(1)
                .setEventLoopPoolSize(1)
                .setPreferNativeTransport(true)
                .setAddressResolverOptions(
                    new AddressResolverOptions().setServers(reachableNameServers)));

    Provider dagger = DaggerProvider.create();
    dagger.init();

    Runtime.getRuntime()
        .addShutdownHook(
            ShutdownHookProvider.get(
                vertx, dagger.providesServiceLifecycleManagement().closeables()));

    DeploymentOptions deploymentOptions =
        new DeploymentOptions().setInstances(config.verticleConfig().numberOfInstances());

    // here we are creating the dependencies for the worker verticle on the event loop
    // we need to move the creation of all these objects onto the worker thread in the
    // worker's verticle start method
    // todo: the verticle should accept the dagger provider as an argument
    //  and use that to create all the dependencies
    vertx
        .deployVerticle(dagger::provideNewApiVerticle, deploymentOptions)
        .onFailure(
            err -> {
              log.error("error while deploying api verticle", err);
              vertx.close().onComplete(ignore -> System.exit(1));
            })
        .onSuccess(id -> log.info("api deployment id: " + id));
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

  @Provides
  static Config.PostgresConfig providesPostgresConfig() {
    return config.postgresConfig();
  }

  @Provides
  static Config.VerticleConfig providesVerticleConfig() {
    return config.verticleConfig();
  }

  @Provides
  static Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig>
      providesServiceRegistryConfig() {
    return config.serviceRegistryConfig();
  }
}
