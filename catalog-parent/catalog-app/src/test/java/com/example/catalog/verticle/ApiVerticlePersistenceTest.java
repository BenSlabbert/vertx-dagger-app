/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.catalog.PersistenceTest;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApiVerticlePersistenceTest extends PersistenceTest {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticlePersistenceTest.class);

  @Test
  void nextPage(VertxTestContext testContext) {
    persist(
        conn -> {
          ItemRepository itemRepository = provider.itemRepository();

          final int size = 100;
          List<Future<?>> futures = new ArrayList<>(size);

          for (int i = 0; i < size; i++) {
            futures.add(itemRepository.create(conn, "name1", 1L));
          }

          return Future.all(futures).map(cf -> null);
        });

    provider
        .itemService()
        .nextPage(0L, 3)
        .onComplete(
            testContext.succeeding(
                page ->
                    testContext.verify(
                        () -> {
                          assertThat(page.more()).isTrue();
                          assertThat(page.items())
                              .hasSize(3)
                              .satisfiesExactly(
                                  item -> assertThat(item.id()).isEqualTo(1L),
                                  item -> assertThat(item.id()).isEqualTo(2L),
                                  item -> assertThat(item.id()).isEqualTo(3L));
                          testContext.completeNow();
                        })));
  }

  @Test
  void previousPage(VertxTestContext testContext) {
    persist(
        conn -> {
          ItemRepository itemRepository = provider.itemRepository();

          final int size = 100;
          List<Future<?>> futures = new ArrayList<>(size);

          for (int i = 0; i < size; i++) {
            futures.add(itemRepository.create(conn, "name1", 1L));
          }

          return Future.all(futures).map(cf -> null);
        });

    provider
        .itemService()
        .previousPage(50L, 3)
        .onComplete(
            testContext.succeeding(
                page ->
                    testContext.verify(
                        () -> {
                          assertThat(page.more()).isTrue();
                          assertThat(page.items())
                              .hasSize(3)
                              .satisfiesExactly(
                                  item -> assertThat(item.id()).isEqualTo(47L),
                                  item -> assertThat(item.id()).isEqualTo(48L),
                                  item -> assertThat(item.id()).isEqualTo(49L));
                          testContext.completeNow();
                        })));
  }

  @Test
  @Disabled("fix authentication")
  void apiTest() {
    log.info("starting apiTest");
    persist(conn -> provider.itemRepository().create(conn, "name1", 1L));

    String createResponseJson =
        RestAssured.given()
            .header(AUTHORIZATION.toString(), "Bearer fake")
            .contentType(ContentType.JSON)
            .body(new CreateItemRequestDto("name2", 2L).toJson().encode())
            .post("/api/create")
            .then()
            .assertThat()
            .statusCode(CREATED.code())
            .extract()
            .asString();

    var createItemResponseDto = new CreateItemResponseDto(new JsonObject(createResponseJson));

    String getResponseJson =
        RestAssured.given()
            .header(AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/" + createItemResponseDto.id())
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    var findOneResponseDto = new FindOneResponseDto(new JsonObject(getResponseJson));
    assertThat(findOneResponseDto).isNotNull();

    String getAllResponseJson =
        RestAssured.given()
            .header(AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/next?fromId=0&size=1")
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    var paginatedResponseDto = new PaginatedResponseDto(new JsonObject(getAllResponseJson));
    assertThat(paginatedResponseDto.more()).isTrue();
    assertThat(paginatedResponseDto.items()).hasSize(1);

    getAllResponseJson =
        RestAssured.given()
            .header(AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/next?fromId=%d&size=1".formatted(paginatedResponseDto.items().get(0).id()))
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    paginatedResponseDto = new PaginatedResponseDto(new JsonObject(getAllResponseJson));
    assertThat(paginatedResponseDto.more()).isFalse();
    assertThat(paginatedResponseDto.items()).hasSize(1);

    String suggestResponseJson =
        RestAssured.given()
            .header(AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/suggest?s=name")
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    var suggestResponseDto = new SuggestResponseDto(new JsonObject(suggestResponseJson));
    assertThat(suggestResponseDto.suggestions())
        .singleElement()
        .satisfies(s -> assertThat(s).isEqualTo("name2"));

    RestAssured.given()
        .header(AUTHORIZATION.toString(), "Bearer fake")
        .contentType(ContentType.JSON)
        .body(
            new UpdateItemRequestDto("name2updated", 3L, createItemResponseDto.version())
                .toJson()
                .encode())
        .post("/api/" + createItemResponseDto.id())
        .then()
        .assertThat()
        .statusCode(NO_CONTENT.code());

    getResponseJson =
        RestAssured.given()
            .header(AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/" + createItemResponseDto.id())
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    findOneResponseDto = new FindOneResponseDto(new JsonObject(getResponseJson));
    assertThat(findOneResponseDto).isNotNull();
    assertThat(findOneResponseDto.priceInCents()).isEqualTo(3L);
    assertThat(findOneResponseDto.name()).isEqualTo("name2updated");
    assertThat(findOneResponseDto.version()).isEqualTo(createItemResponseDto.version() + 1L);

    RestAssured.given()
        .header(AUTHORIZATION.toString(), "Bearer fake")
        .delete("/api/" + createItemResponseDto.id())
        .then()
        .assertThat()
        .statusCode(NO_CONTENT.code());

    RestAssured.given()
        .header(AUTHORIZATION.toString(), "Bearer fake")
        .get("/api/" + createItemResponseDto.id())
        .then()
        .assertThat()
        .statusCode(NOT_FOUND.code());
  }
}
