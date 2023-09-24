/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.ioc.DaggerTestMockRepositoryProvider;
import com.example.catalog.ioc.TestMockRepositoryProvider;
import com.example.catalog.repository.SuggestionService;
import com.example.commons.HttpServerTest;
import com.example.commons.config.Config;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.pgclient.PgPool;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
public abstract class MockRepositoryTest extends HttpServerTest {

  protected static final int PORT = getPort();

  protected TestMockRepositoryProvider provider;

  protected SuggestionService suggestionService;
  protected DSLContext dslContext;
  protected PgPool pgPool;

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    AuthenticationIntegration authHandler = Mockito.mock(AuthenticationIntegration.class);
    when(authHandler.isTokenValid(anyString())).thenReturn(Future.succeededFuture(true));

    suggestionService = Mockito.mock(SuggestionService.class);
    pgPool = Mockito.mock(PgPool.class);
    dslContext = Mockito.mock(DSLContext.class);

    Config config =
        new Config(
            new Config.HttpConfig(PORT),
            new Config.GrpcConfig(50500),
            new Config.RedisConfig("localhost", 6379, 0),
            new Config.PostgresConfig("localhost", 5432, "postgres", "postgres", "postgres"),
            Map.of(),
            new Config.VerticleConfig(1));

    provider =
        DaggerTestMockRepositoryProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .verticleConfig(config.verticleConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .authenticationIntegration(authHandler)
            .suggestionService(suggestionService)
            .pgPool(pgPool)
            .closeables(Set.of())
            .dslContext(dslContext)
            .build();

    vertx.deployVerticle(provider.provideNewApiVerticle(), testContext.succeedingThenComplete());
  }

  @AfterEach
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
