/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import static com.example.commons.FreePortUtility.getPort;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.commons.config.Config;
import com.example.payment.ioc.DaggerTestMockPersistenceProvider;
import com.example.payment.ioc.TestMockPersistenceProvider;
import com.example.payment.repository.PaymentRepository;
import io.restassured.RestAssured;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import lombok.extern.java.Log;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.TransactionalCallable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@Log
@ExtendWith(VertxExtension.class)
public abstract class MockPersistenceTest {

  protected static final int HTTP_PORT = getPort();
  protected static final int GRPC_PORT = getPort();

  protected TestMockPersistenceProvider provider;
  protected DSLContext dslContext = mock(DSLContext.class);
  protected PaymentRepository paymentRepository = mock(PaymentRepository.class);
  protected DataSource dataSource = mock(DataSource.class);

  @BeforeAll
  static void beforeAll() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @AfterAll
  static void afterAll() {
    log.info("tests ended");
  }

  @BeforeEach
  void prepare(Vertx vertx) {
    Config config =
        new Config(
            new Config.HttpConfig(HTTP_PORT),
            new Config.GrpcConfig(GRPC_PORT),
            Config.RedisConfig.builder().build(),
            new Config.PostgresConfig("127.0.0.1", 5432, "postgres", "postgres", "postgres"),
            null,
            Map.of(),
            new Config.VerticleConfig(1));

    provider =
        DaggerTestMockPersistenceProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .verticleConfig(config.verticleConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .postgresConfig(config.postgresConfig())
            .paymentRepository(paymentRepository)
            .dslContext(dslContext)
            .dataSource(dataSource)
            .closeables(Set.of())
            .build();
    provider.init();

    when(dslContext.transactionResult(any(TransactionalCallable.class)))
        .thenAnswer(
            invocation -> {
              Object[] arguments = invocation.getArguments();
              TransactionalCallable<?> transactionalCallable =
                  (TransactionalCallable<?>) arguments[0];

              Configuration configuration = mock(Configuration.class);
              when(configuration.dsl()).thenReturn(dslContext);
              return transactionalCallable.run(configuration);
            });
  }

  @BeforeEach
  void before() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = HTTP_PORT;
  }

  @AfterEach
  void after() {
    RestAssured.reset();
  }
}
