package com.example.starter.route.handler.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record LoginRequestDto(String username, String password) {

  public LoginRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("username")),
        Objects.requireNonNull(jsonObject.getString("password")));
  }

  public JsonObject toJson() {
    return new JsonObject().put("username", username).put("password", password);
  }
}
