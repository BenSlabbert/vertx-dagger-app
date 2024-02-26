/* Licensed under Apache-2.0 2024. */
package com.example.warehouse;

import static com.example.commons.FreePortUtility.getPort;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.commons.ConfigEncoder;
import com.example.commons.TestcontainerLogConsumer;
import com.example.commons.config.Config;
import com.example.iam.rpc.api.IamRpcServiceAuthenticationProvider;
import com.example.migration.FlywayProvider;
import com.example.warehouse.ioc.DaggerTestProvider;
import com.example.warehouse.ioc.TestProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.Credentials;
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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(VertxExtension.class)
public abstract class PersistenceTest {

  private static final Logger log = LoggerFactory.getLogger(PersistenceTest.class);
  protected static final int HTTP_PORT = getPort();
  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final Network network = Network.newNetwork();

  protected TestProvider provider;

  protected static final GenericContainer<?> postgres =
      new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withExposedPorts(5432)
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withEnv("POSTGRES_USER", "postgres")
          .withEnv("POSTGRES_PASSWORD", "postgres")
          .withEnv("POSTGRES_DB", "postgres")
          // must wait twice as the init process also prints this message
          .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
          .withLogConsumer(new TestcontainerLogConsumer("postgres"));

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

    var mockAuthProvider = mock(IamRpcServiceAuthenticationProvider.class);

    when(mockAuthProvider.authenticate(any(Credentials.class)))
        .thenReturn(Future.succeededFuture(User.fromName("user")));

    provider =
        DaggerTestProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .postgresConfig(config.postgresConfig())
            .iamRpcServiceAuthenticationProvider(mockAuthProvider)
            .build();

    provider.init();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.warehouseVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
