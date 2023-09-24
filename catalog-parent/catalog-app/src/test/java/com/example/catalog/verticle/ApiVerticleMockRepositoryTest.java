/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.catalog.MockRepositoryTest;
import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import io.restassured.RestAssured;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ApiVerticleMockRepositoryTest extends MockRepositoryTest {

  @Test
  void getById() {
    when(itemRepository.findById(any(), eq(1L)))
        .thenReturn(
            Future.succeededFuture(
                Optional.of(
                    ItemProjection.builder().id(1L).name("name").priceInCents(123L).build())));

    String getResponseJson =
        RestAssured.given()
            .header(HttpHeaders.AUTHORIZATION.toString(), "Bearer fake")
            .get("/api/1")
            .then()
            .assertThat()
            .statusCode(OK.code())
            .extract()
            .asString();

    FindOneResponseDto findOneResponseDto = new FindOneResponseDto(new JsonObject(getResponseJson));
    assertThat(findOneResponseDto).isNotNull();

    verify(itemRepository).findById(any(), eq(1L));
  }
}
