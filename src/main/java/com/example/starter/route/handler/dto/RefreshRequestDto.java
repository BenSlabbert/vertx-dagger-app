package com.example.starter.route.handler.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RefreshRequestDto(String username, String token) {

  public RefreshRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("username")),
        Objects.requireNonNull(jsonObject.getString("token")));
  }

  public JsonObject toJson() {
    return new JsonObject().put("username", username).put("token", token);
  }
}
