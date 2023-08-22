package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.vertx.core.http.HttpMethod.GET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.catalog.TestProvider;
import com.example.catalog.service.ItemService;
import com.example.catalog.web.SchemaValidatorDelegator;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.handler.ItemHandler;
import com.example.commons.config.Config;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
class ApiVerticleTest extends TestProvider {

  private static final int PORT = 40001;

  private final ItemService itemService = Mockito.mock(ItemService.class);

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    var httpConfig = new Config.HttpConfig(PORT);
    vertx.deployVerticle(
        new ApiVerticle(
            httpConfig,
            new ItemHandler(itemService, new SchemaValidatorDelegator(httpConfig)),
            () -> RoutingContext::next),
        testContext.succeedingThenComplete());
  }

  @Test
  void getItemsTest(Vertx vertx, VertxTestContext testContext) {
    FindOneResponseDto responseDto = new FindOneResponseDto(UUID.randomUUID(), 1L, "name", 123L);
    when(itemService.findAll(0, 10, ItemService.Direction.FORWARD))
        .thenReturn(
            Future.succeededFuture(
                new PaginatedResponseDto(
                    false, List.of(responseDto).size(), List.of(responseDto))));

    vertx
        .createHttpClient()
        .request(GET, PORT, "localhost", "/api/items?from=0&to=10")
        .compose(HttpClientRequest::send)
        .onComplete(
            testContext.succeeding(
                clientResponse ->
                    testContext.verify(
                        () -> {
                          assertThat(clientResponse.statusCode()).isEqualTo(OK.code());
                          clientResponse
                              .body()
                              .onFailure(testContext::failNow)
                              .onSuccess(
                                  buff -> {
                                    PaginatedResponseDto paginatedResponseDto =
                                        new PaginatedResponseDto(new JsonObject(buff));
                                    assertThat(paginatedResponseDto).isNotNull();
                                    assertThat(paginatedResponseDto.items())
                                        .singleElement()
                                        .usingRecursiveComparison()
                                        .isEqualTo(responseDto);
                                    testContext.completeNow();
                                  });
                        })));
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
