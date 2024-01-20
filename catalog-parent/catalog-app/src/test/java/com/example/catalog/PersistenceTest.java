/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import static com.example.commons.FreePortUtility.getPort;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.ioc.DaggerTestPersistenceProvider;
import com.example.catalog.ioc.TestPersistenceProvider;
import com.example.catalog.verticle.ApiVerticle;
import com.example.commons.ConfigEncoder;
import com.example.commons.TestcontainerLogConsumer;
import com.example.commons.config.Config;
import com.example.commons.transaction.reactive.TransactionBoundary;
import com.example.iam.rpc.api.CheckTokenResponse;
import com.example.iam.rpc.api.IamRpcService;
import com.example.iam.rpc.api.IamRpcServiceVertxProxyHandler;
import com.example.migration.FlywayProvider;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlClient;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

  protected TestPersistenceProvider provider;

  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final Network network = Network.newNetwork();

  private MessageConsumer<JsonObject> register;

  protected static final GenericContainer<?> redis =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
          .withLogConsumer(new TestcontainerLogConsumer("redis"));

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
    log.info("starting redis");
    log.info("starting postgres");
    Startables.deepStart(redis, postgres).join();
    log.info("done");
  }

  @BeforeAll
  static void beforeAll() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
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

    AuthenticationIntegration authHandler = mock(AuthenticationIntegration.class);
    when(authHandler.isTokenValid(anyString()))
        .thenReturn(
            Future.succeededFuture(
                CheckTokenResponse.builder()
                    .valid(true)
                    .userPrincipal(JsonObject.of().encode())
                    .userAttributes(JsonObject.of().encode())
                    .build()));

    Config config =
        new Config(
            new Config.HttpConfig(HTTP_PORT),
            new Config.RedisConfig("127.0.0.1", redis.getMappedPort(6379), 0),
            new Config.PostgresConfig(
                "127.0.0.1", postgres.getMappedPort(5432), "postgres", "postgres", dbName),
            new Config.VerticleConfig(1));

    provider =
        DaggerTestPersistenceProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .postgresConfig(config.postgresConfig())
            .verticleConfig(config.verticleConfig())
            .authenticationIntegration(authHandler)
            .build();
    provider.init();

    register =
        new IamRpcServiceVertxProxyHandler(
                vertx,
                request ->
                    Future.succeededFuture(
                        CheckTokenResponse.builder()
                            .valid(true)
                            .userAttributes(new JsonObject().encode())
                            .userPrincipal(new JsonObject().encode())
                            .build()))
            .register(vertx.eventBus(), IamRpcService.ADDRESS);

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        new ApiVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }

  @BeforeEach
  void before() {
    RestAssured.baseURI = "http://127.0.0.1";
    RestAssured.port = HTTP_PORT;
  }

  @AfterEach
  void after() {
    register.unregister();
    RestAssured.reset();
    for (AutoCloseable closeable : provider.closeables()) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.warn("failed to close " + closeable);
      }
    }
  }

  protected <T> void persist(Function<SqlClient, Future<T>> function) {
    Pool pool = provider.pool();
    CountDownLatch latch = new CountDownLatch(1);

    new TransactionBoundary(pool) {
      {
        doInTransaction(function)
            .onFailure(err -> fail("failure while persisting", err))
            .onSuccess(projection -> latch.countDown());
      }
    };

    try {
      latch.await();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail("failure while waiting for persistence to complete", e);
    }
  }
}
