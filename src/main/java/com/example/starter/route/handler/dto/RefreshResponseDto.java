package com.example.starter.route.handler.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RefreshResponseDto(String token, String refreshToken) {

  public RefreshResponseDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("token")),
        Objects.requireNonNull(jsonObject.getString("refreshToken")));
  }

  public JsonObject toJson() {
    return new JsonObject().put("token", token).put("refreshToken", refreshToken);
  }
}
