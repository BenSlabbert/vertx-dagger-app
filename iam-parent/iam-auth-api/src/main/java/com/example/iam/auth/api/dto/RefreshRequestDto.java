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

public record RefreshRequestDto(String username, String token) implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String TOKEN_FIELD = "token";

  private static final JsonSchema SCHEMA =
      JsonSchema.of(
          objectSchema()
              .requiredProperty(USERNAME_FIELD, stringSchema().with(minLength(1)))
              .requiredProperty(TOKEN_FIELD, stringSchema().with(minLength(1)))
              .toJson());

  public RefreshRequestDto(JsonObject jsonObject) {
    this(jsonObject.getString(USERNAME_FIELD), jsonObject.getString(TOKEN_FIELD));
  }

  public RefreshRequestDto {
    Objects.requireNonNull(username);
    Objects.requireNonNull(token);
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(USERNAME_FIELD, username).put(TOKEN_FIELD, token);
  }

  public static JsonSchema getSchema() {
    return SCHEMA;
  }

  public static Builder builder() {
    return new AutoBuilder_RefreshRequestDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder username(String username);

    Builder token(String token);

    RefreshRequestDto build();
  }
}
