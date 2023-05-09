package com.example.iam.web.route.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RefreshRequestDto(String username, String token) implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String TOKEN_FIELD = "token";

  public RefreshRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(USERNAME_FIELD)),
        Objects.requireNonNull(jsonObject.getString(TOKEN_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(USERNAME_FIELD, username).put(TOKEN_FIELD, token);
  }
}
