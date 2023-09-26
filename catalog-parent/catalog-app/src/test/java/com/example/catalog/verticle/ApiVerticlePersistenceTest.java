/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.catalog.PersistenceTest;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class ApiVerticlePersistenceTest extends PersistenceTest {

  @Test
  void apiTest() {
    persist(conn -> provider.itemRepository().create(conn, "name1", 1L));

    String createResponseJson =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
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
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
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
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
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
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
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
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
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
  }
}
