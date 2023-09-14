/* Licensed under Apache-2.0 2023. */
package com.example.iam.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record LoginResponseDto(String token, String refreshToken) implements JsonWriter {

  public static String TOKEN_FIELD = "token";
  public static String REFRESH_TOKEN_FIELD = "refreshToken";

  public LoginResponseDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(TOKEN_FIELD)),
        Objects.requireNonNull(jsonObject.getString(REFRESH_TOKEN_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(TOKEN_FIELD, token).put(REFRESH_TOKEN_FIELD, refreshToken);
  }
}
