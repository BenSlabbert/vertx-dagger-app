/* Licensed under Apache-2.0 2024. */
package com.example.reactivetest.verticle;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.ConfigEncoder;
import com.example.commons.config.Config;
import com.example.commons.config.Config.HttpConfig;
import com.example.commons.config.Config.PostgresConfig;
import com.example.commons.docker.DockerContainers;
import com.example.migration.FlywayProvider;
import com.example.reactivetest.ioc.DaggerProvider;
import com.example.reactivetest.ioc.Provider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(VertxExtension.class)
class ApplicationVerticleTest {

  private static final Logger log = LoggerFactory.getLogger(ApplicationVerticleTest.class);

  protected static final int HTTP_PORT = getPort();

  private static final AtomicInteger counter = new AtomicInteger(0);

  protected Provider provider;

  protected static final GenericContainer<?> postgres = DockerContainers.POSTGRES;

  // https://testcontainers.com/guides/testcontainers-container-lifecycle/#_using_singleton_containers
  static {
    log.info("starting postgres");
    postgres.start();
    log.info("done");
  }

  @Test
  void test(Vertx vertx, VertxTestContext testContext) throws Exception {
    final String dbName = "testing" + counter.incrementAndGet();
    log.info("creating db: " + dbName);

    // create a new database for each test
    Container.ExecResult execResult =
        postgres.execInContainer("psql", "-U", "postgres", "-c", "CREATE DATABASE " + dbName);
    if (execResult.getExitCode() != 0) {
      testContext.failNow("failed to create database: " + execResult.getStderr());
      return;
    }

    Flyway flyway =
        FlywayProvider.get(
            "127.0.0.1", postgres.getMappedPort(5432), "postgres", "postgres", dbName);
    flyway.clean();
    flyway.migrate();

    Config config =
        Config.builder()
            .httpConfig(HttpConfig.builder().port(HTTP_PORT).build())
            .postgresConfig(
                PostgresConfig.builder()
                    .host("127.0.0.1")
                    .port(postgres.getMappedPort(5432))
                    .database(dbName)
                    .host("postgres")
                    .username("postgres")
                    .password("postgres")
                    .build())
            .build();

    provider = DaggerProvider.builder().vertx(vertx).config(config).build();
    provider.init();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.applicationVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
