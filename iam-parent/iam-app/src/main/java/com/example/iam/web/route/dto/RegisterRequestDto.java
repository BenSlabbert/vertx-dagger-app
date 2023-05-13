package com.example.iam.web.route.dto;

import com.example.commons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;
import lombok.Builder;

@Builder
public record RegisterRequestDto(String username, String password) implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String PASSWORD_FIELD = "password";

  public RegisterRequestDto(JsonObject jsonObject) {
    this(
        Objects.requireNonNull(jsonObject.getString(USERNAME_FIELD)),
        Objects.requireNonNull(jsonObject.getString(PASSWORD_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(USERNAME_FIELD, username).put(PASSWORD_FIELD, password);
  }
}
