/* Licensed under Apache-2.0 2024. */
package com.example.jdbc;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.ConfigEncoder;
import com.example.commons.config.Config;
import com.example.commons.docker.DockerContainers;
import com.example.jdbc.ioc.DaggerProvider;
import com.example.jdbc.ioc.Provider;
import com.example.migration.FlywayProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

@ExtendWith(VertxExtension.class)
public class IntegrationTestBase {

  private static final GenericContainer<?> postgres = DockerContainers.POSTGRES;
  private static final AtomicInteger counter = new AtomicInteger(0);

  protected Provider provider;
  protected int httpPort;

  static {
    postgres.start();
  }

  @AfterEach
  protected void cleanup() {
    provider.close();
  }

  @BeforeEach
  protected void prepare(Vertx vertx, VertxTestContext testContext) throws Exception {
    httpPort = getPort();
    final String dbName = "testing" + counter.incrementAndGet();

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
            .httpConfig(Config.HttpConfig.builder().port(httpPort).build())
            .postgresConfig(
                Config.PostgresConfig.builder()
                    .host("127.0.0.1")
                    .port(postgres.getMappedPort(5432))
                    .username("postgres")
                    .password("postgres")
                    .database(dbName)
                    .build())
            .build();

    provider =
        DaggerProvider.builder()
            .vertx(vertx)
            .config(config)
            .postgresConfig(config.postgresConfig())
            .httpConfig(config.httpConfig())
            .build();
    provider.init();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.jdbcVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
