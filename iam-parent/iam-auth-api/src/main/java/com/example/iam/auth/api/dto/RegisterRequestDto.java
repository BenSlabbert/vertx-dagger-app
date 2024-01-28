/* Licensed under Apache-2.0 2023. */
package com.example.iam.auth.api.dto;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.commons.web.serialization.JsonWriter;
import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Objects;

public record RegisterRequestDto(String username, String password) implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String PASSWORD_FIELD = "password";

  private static final JsonSchema SCHEMA =
      JsonSchema.of(
          objectSchema()
              .requiredProperty(USERNAME_FIELD, stringSchema().with(minLength(1)))
              .requiredProperty(PASSWORD_FIELD, stringSchema().with(minLength(1)))
              .toJson());

  public RegisterRequestDto(JsonObject jsonObject) {
    this(jsonObject.getString(USERNAME_FIELD), jsonObject.getString(PASSWORD_FIELD));
  }

  public RegisterRequestDto {
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(USERNAME_FIELD, username).put(PASSWORD_FIELD, password);
  }

  public static JsonSchema getSchema() {
    return SCHEMA;
  }

  public static Builder builder() {
    return new AutoBuilder_RegisterRequestDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder username(String username);

    Builder password(String password);

    RegisterRequestDto build();
  }
}
