/* Licensed under Apache-2.0 2023. */
package com.example.iam.entity;

import static java.util.Objects.requireNonNull;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import lombok.Builder;

@Builder
public record User(String username, String password, String refreshToken) implements JsonWriter {

  public static final String USERNAME_FIELD = "username";
  public static final String PASSWORD_FIELD = "password";
  public static final String REFRESH_TOKEN_FIELD = "refreshToken";

  public User(JsonObject jsonObject) {
    this(
        requireNonNull(jsonObject.getString(USERNAME_FIELD)),
        requireNonNull(jsonObject.getString(PASSWORD_FIELD)),
        requireNonNull(jsonObject.getString(REFRESH_TOKEN_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(USERNAME_FIELD, username)
        .put(PASSWORD_FIELD, password)
        .put(REFRESH_TOKEN_FIELD, refreshToken);
  }
}
