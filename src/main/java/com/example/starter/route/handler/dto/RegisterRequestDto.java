package com.example.starter.route.handler.dto;

import com.example.starter.json.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RegisterRequestDto(String username, String password) implements JsonWriter {

  public RegisterRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("username")),
        Objects.requireNonNull(jsonObject.getString("password")));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put("username", username).put("password", password);
  }
}
