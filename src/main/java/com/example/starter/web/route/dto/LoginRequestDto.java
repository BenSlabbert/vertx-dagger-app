package com.example.starter.web.route.dto;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record LoginRequestDto(String username, String password) implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String PASSWORD_FIELD = "password";

  public LoginRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(USERNAME_FIELD)),
        Objects.requireNonNull(jsonObject.getString(PASSWORD_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(USERNAME_FIELD, username).put(PASSWORD_FIELD, password);
  }
}
