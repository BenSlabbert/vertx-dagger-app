package com.example.starter.web.route.dto;

import com.example.starter.web.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record LoginResponseDto(String token, String refreshToken) implements JsonWriter {

  public LoginResponseDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString("token")),
        Objects.requireNonNull(jsonObject.getString("refreshToken")));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put("token", token).put("refreshToken", refreshToken);
  }
}
