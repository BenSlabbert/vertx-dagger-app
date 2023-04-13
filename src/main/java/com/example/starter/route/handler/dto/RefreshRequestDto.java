package com.example.starter.route.handler.dto;

import com.example.starter.json.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RefreshRequestDto(String username, String token) implements JsonWriter {

  public RefreshRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("username")),
        Objects.requireNonNull(jsonObject.getString("token")));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put("username", username).put("token", token);
  }
}
