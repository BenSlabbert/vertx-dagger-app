/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.vertx.core.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.catalog.PersistenceTest;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class ApiVerticlePersistenceTest extends PersistenceTest {

  private static final Logger log = LoggerFactory.getLogger(ApiVerticlePersistenceTest.class);

  @Test
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
            .get("/api/items?lastId=0&size=1")
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
            .get("/api/items?lastId=%d&size=1".formatted(paginatedResponseDto.items().get(0).id()))
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
        .post("/api/edit/" + createItemResponseDto.id())
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
