/* Licensed under Apache-2.0 2023. */
package com.example.iam.auth.api.dto;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.iam.auth.api.perms.Access;
import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxdaggercommons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Objects;

public record RegisterRequestDto(String username, String password, Access access)
    implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String PASSWORD_FIELD = "password";
  public static String ACCESS_FIELD = "access";

  public static final JsonSchema SCHEMA =
      JsonSchema.of(
          objectSchema()
              .requiredProperty(USERNAME_FIELD, stringSchema().with(minLength(1)))
              .requiredProperty(PASSWORD_FIELD, stringSchema().with(minLength(1)))
              .requiredProperty(ACCESS_FIELD, objectSchema())
              .toJson());

  public RegisterRequestDto(JsonObject jsonObject) {
    this(
        jsonObject.getString(USERNAME_FIELD),
        jsonObject.getString(PASSWORD_FIELD),
        Access.fromJson(jsonObject.getJsonObject(ACCESS_FIELD)));
  }

  public RegisterRequestDto {
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);
    Objects.requireNonNull(access);
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject()
        .put(USERNAME_FIELD, username)
        .put(PASSWORD_FIELD, password)
        .put(ACCESS_FIELD, access.toJson());
  }

  public static Builder builder() {
    return new AutoBuilder_RegisterRequestDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder username(String username);

    Builder password(String password);

    Builder access(Access access);

    RegisterRequestDto build();
  }
}
