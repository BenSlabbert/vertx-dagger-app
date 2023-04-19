package com.example.starter.web.route.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RefreshResponseDto(String token, String refreshToken) implements JsonWriter {

  public static String TOKEN_FIELD = "token";
  public static String REFRESH_TOKEN_FIELD = "refreshToken";

  public RefreshResponseDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(TOKEN_FIELD)),
        Objects.requireNonNull(jsonObject.getString(REFRESH_TOKEN_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(TOKEN_FIELD, token).put(REFRESH_TOKEN_FIELD, refreshToken);
  }
}
