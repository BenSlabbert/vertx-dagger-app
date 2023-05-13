package com.example.catalog;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

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
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.java.Log;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

@Log
@Module
public class Main {

  private static Config config;
  private static Vertx vertx;

  public static void main(String[] args) throws IOException {
    log.log(INFO, "starting app: {0}", new Object[] {Arrays.toString(args)});

    config = ParseConfig.parseArgs(args);

    Flyway flyway =
        Flyway.configure()
            .dataSource("jdbc:postgresql://localhost:5432/db", "user", "password")
            .load();

    // Start the migration
    MigrateResult result = flyway.migrate();
    if (!result.success) {
      log.severe("failed to migrate db");
      System.exit(1);
    }

    List<String> reachableNameServers = ReachableNameServers.getReachableNameServers();
    log.log(INFO, "reachableNameServers: {0}", new Object[] {reachableNameServers});

    vertx =
        Vertx.vertx(
            new VertxOptions()
                .setPreferNativeTransport(true)
                .setAddressResolverOptions(
                    new AddressResolverOptions().setServers(reachableNameServers)));

    Provider dagger = DaggerProvider.create();

    Runtime.getRuntime().addShutdownHook(ShutdownHookProvider.get(vertx, List.of()));

    DeploymentOptions deploymentOptions =
        new DeploymentOptions().setInstances(config.verticleConfig().numberOfInstances());

    vertx
        .deployVerticle(dagger::provideNewApiVerticle, deploymentOptions)
        .onFailure(throwable -> log.log(SEVERE, "error while deploying api verticle", throwable))
        .onSuccess(id -> log.log(INFO, "api deployment id: {0}", new Object[] {id}));
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
  static Config.VerticleConfig providesVerticleConfig() {
    return config.verticleConfig();
  }
}
