package com.example.starter.route.handler.dto;

import com.example.starter.json.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record LoginRequestDto(String username, String password) implements JsonWriter {

  public LoginRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("username")),
        Objects.requireNonNull(jsonObject.getString("password")));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put("username", username).put("password", password);
  }
}
