/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.catalog.ioc.DaggerTestPersistenceProvider;
import com.example.catalog.ioc.TestPersistenceProvider;
import com.example.catalog.verticle.ApiVerticle;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcServiceVertxEBProxyHandler;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.dto.CheckTokenResponseDto;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.config.Config.RedisConfig;
import github.benslabbert.vertxdaggercommons.dbmigration.FlywayProvider;
import github.benslabbert.vertxdaggercommons.test.ConfigEncoder;
import github.benslabbert.vertxdaggercommons.test.DockerContainers;
import github.benslabbert.vertxdaggercommons.transaction.reactive.TransactionBoundary;
import io.restassured.RestAssured;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startables;

@ExtendWith(VertxExtension.class)
public abstract class PersistenceTest {

  private static final Logger log = LoggerFactory.getLogger(PersistenceTest.class);

  protected TestPersistenceProvider provider;

  private static final AtomicInteger counter = new AtomicInteger(0);

  private MessageConsumer<JsonObject> register;

  protected static final GenericContainer<?> redis = DockerContainers.REDIS;
  protected static final GenericContainer<?> postgres = DockerContainers.POSTGRES;

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

    IamRpcService authHandler = mock(IamRpcService.class);
    when(authHandler.check(any()))
        .thenReturn(
            Future.succeededFuture(
                CheckTokenResponseDto.builder()
                    .valid(true)
                    .userPrincipal(JsonObject.of().encode())
                    .userAttributes(JsonObject.of().encode())
                    .build()));

    Config config =
        Config.builder()
            .httpConfig(Config.HttpConfig.builder().port(0).build())
            .redisConfig(
                RedisConfig.builder()
                    .host("127.0.0.1")
                    .port(redis.getMappedPort(6379))
                    .database(0)
                    .build())
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
        DaggerTestPersistenceProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .postgresConfig(config.postgresConfig())
            .iamRpcService(authHandler)
            .build();
    provider.init();

    register =
        new IamRpcServiceVertxEBProxyHandler(
                vertx,
                ignore ->
                    Future.succeededFuture(
                        CheckTokenResponseDto.builder()
                            .valid(true)
                            .userAttributes(new JsonObject().encode())
                            .userPrincipal(new JsonObject().encode())
                            .build()))
            .register(vertx.eventBus(), IamRpcService.ADDRESS);

    JsonObject cfg = ConfigEncoder.encode(config);
    ApiVerticle verticle = provider.apiVerticle();
    vertx.deployVerticle(
        verticle,
        new DeploymentOptions().setConfig(cfg))
      .onComplete(        ar -> {
          if (ar.succeeded()) {
            RestAssured.baseURI = "http://127.0.0.1";
            RestAssured.port = verticle.getPort();
            testContext.completeNow();
          } else {
            testContext.failNow(ar.cause());
          }
        });
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
            .onSuccess(ignore -> latch.countDown());
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
