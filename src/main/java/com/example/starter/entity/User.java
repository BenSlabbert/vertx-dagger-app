package com.example.starter.entity;

import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record User(String username, String password, String refreshToken) {

  public static final String USERNAME_FIELD = "username";
  public static final String PASSWORD_FIELD = "password";
  public static final String REFRESH_TOKEN_FIELD = "refreshToken";

  public User(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(USERNAME_FIELD)),
        Objects.requireNonNull(jsonObject.getString(PASSWORD_FIELD)),
        Objects.requireNonNull(jsonObject.getString(REFRESH_TOKEN_FIELD)));
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put(USERNAME_FIELD, username)
        .put(PASSWORD_FIELD, password)
        .put(REFRESH_TOKEN_FIELD, refreshToken);
  }
}
