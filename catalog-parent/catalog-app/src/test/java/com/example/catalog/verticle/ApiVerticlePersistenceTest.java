/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.catalog.PersistenceTest;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

class ApiVerticlePersistenceTest extends PersistenceTest {

  @Test
  void apiTest() {
    String createResponseJson =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
            .body(new CreateItemRequestDto("name", 123L).toJson().encode())
            .post("/api/create")
            .then()
            .assertThat()
            .statusCode(CREATED.code())
            .extract()
            .asString();

    CreateItemResponseDto createItemResponseDto =
        new CreateItemResponseDto(new JsonObject(createResponseJson));

    String getResponseJson =
        RestAssured.given()
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/" + createItemResponseDto.id())
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    FindOneResponseDto findOneResponseDto = new FindOneResponseDto(new JsonObject(getResponseJson));
    assertThat(findOneResponseDto).isNotNull();
  }
}
