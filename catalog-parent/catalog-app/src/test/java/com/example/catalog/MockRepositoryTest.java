/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import static com.example.commons.FreePortUtility.getPort;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.ioc.DaggerTestMockRepositoryProvider;
import com.example.catalog.ioc.TestMockRepositoryProvider;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.SuggestionService;
import com.example.commons.config.Config;
import com.example.iam.rpc.api.CheckTokenResponse;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import java.util.Set;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class MockRepositoryTest {

  protected static final int HTTP_PORT = getPort();

  protected TestMockRepositoryProvider provider;

  protected SuggestionService suggestionService = mock(SuggestionService.class);
  protected ItemRepository itemRepository = mock(ItemRepository.class);
  protected DSLContext dslContext = mock(DSLContext.class);
  protected RedisAPI redisAPI = mock(RedisAPI.class);
  protected Pool pool = mock(Pool.class);

  @BeforeEach
  void prepare(Vertx vertx) {
    AuthenticationIntegration authHandler = mock(AuthenticationIntegration.class);
    when(authHandler.isTokenValid(anyString()))
        .thenReturn(
            Future.succeededFuture(
                CheckTokenResponse.builder()
                    .valid(true)
                    .userPrincipal(JsonObject.of().encode())
                    .userAttributes(JsonObject.of().encode())
                    .build()));

    // needed for transaction boundary
    SqlConnection sqlConnection = mock(SqlConnection.class);
    Transaction transaction = mock(Transaction.class);

    when(redisAPI.ping(any())).thenReturn(Future.succeededFuture(null));

    when(pool.getConnection()).thenReturn(Future.succeededFuture(sqlConnection));
    when(sqlConnection.begin()).thenReturn(Future.succeededFuture(transaction));
    when(sqlConnection.close()).thenReturn(Future.succeededFuture());
    when(transaction.commit()).thenReturn(Future.succeededFuture());

    Config config =
        new Config(
            new Config.HttpConfig(HTTP_PORT),
            new Config.RedisConfig("127.0.0.1", 6379, 0),
            new Config.PostgresConfig("127.0.0.1", 5432, "postgres", "postgres", "postgres"),
            new Config.VerticleConfig(1));

    provider =
        DaggerTestMockRepositoryProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .redisConfig(config.redisConfig())
            .postgresConfig(config.postgresConfig())
            .verticleConfig(config.verticleConfig())
            .authenticationIntegration(authHandler)
            .suggestionService(suggestionService)
            .itemRepository(itemRepository)
            .pool(pool)
            .redisAPI(redisAPI)
            .closeables(Set.of())
            .dslContext(dslContext)
            .build();
    provider.init();
  }
}
