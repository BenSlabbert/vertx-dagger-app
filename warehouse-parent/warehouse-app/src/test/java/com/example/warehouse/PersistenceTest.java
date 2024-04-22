/* Licensed under Apache-2.0 2024. */
package com.example.warehouse;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.ConfigEncoder;
import com.example.commons.config.Config;
import com.example.commons.docker.DockerContainers;
import com.example.commons.security.rpc.ACL;
import com.example.migration.FlywayProvider;
import com.example.warehouse.ioc.DaggerTestProvider;
import com.example.warehouse.ioc.TestProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;

@ExtendWith(VertxExtension.class)
public abstract class PersistenceTest {

  private static final Logger log = LoggerFactory.getLogger(PersistenceTest.class);
  protected static final int HTTP_PORT = getPort();
  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final Network network = Network.newNetwork();

  protected TestProvider provider;
  protected String validJwtToken;
  protected String invalidJwtToken;

  protected static final GenericContainer<?> postgres = DockerContainers.POSTGRES;

  // https://testcontainers.com/guides/testcontainers-container-lifecycle/#_using_singleton_containers
  static {
    log.info("starting postgres");
    Startables.deepStart(postgres).join();
    log.info("done");
  }

  @AfterAll
  static void afterAll() {
    log.info("tests ended, stopping containers");
  }

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) throws Exception {
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
            .httpConfig(Config.HttpConfig.builder().port(HTTP_PORT).build())
            .postgresConfig(
                Config.PostgresConfig.builder()
                    .host("127.0.0.1")
                    .port(postgres.getMappedPort(5432))
                    .username("postgres")
                    .password("postgres")
                    .database(dbName)
                    .build())
            .build();

    JWTAuth jwtAuth =
        JWTAuth.create(
            vertx,
            new JWTAuthOptions()
                .addPubSecKey(
                    new PubSecKeyOptions()
                        .setId("authKey1")
                        .setAlgorithm("HS256")
                        .setBuffer("password")));

    validJwtToken =
        jwtAuth.generateToken(
            // this is the root claim
            new JsonObject()
                .put(
                    "acl",
                    ACL.builder()
                        .group("warehouse")
                        .role("worker")
                        .addPermission("read")
                        .build()
                        .toJson()),
            new JWTOptions()
                .setExpiresInSeconds(Integer.MAX_VALUE)
                .setIssuer("iam")
                .setSubject("sneaky"));
    invalidJwtToken =
        jwtAuth.generateToken(
            // this is the root claim
            new JsonObject()
                .put(
                    "acl",
                    ACL.builder()
                        .group("ui")
                        .role("customer")
                        .addPermission("read")
                        .build()
                        .toJson()),
            new JWTOptions()
                .setExpiresInSeconds(Integer.MAX_VALUE)
                .setIssuer("iam")
                .setSubject("sneaky"));

    provider =
        DaggerTestProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .postgresConfig(config.postgresConfig())
            .authenticationProvider(jwtAuth)
            .build();

    provider.init();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.warehouseVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
