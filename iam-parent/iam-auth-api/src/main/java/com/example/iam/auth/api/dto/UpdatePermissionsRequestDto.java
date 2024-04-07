/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.dto;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.commons.web.serialization.JsonWriter;
import com.example.iam.auth.api.perms.Access;
import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;

public record UpdatePermissionsRequestDto(String username, Access access) implements JsonWriter {

  public static String USERNAME_FIELD = "username";
  public static String ACCESS_FIELD = "access";

  public static final JsonSchema SCHEMA =
      JsonSchema.of(
          objectSchema()
              .requiredProperty(USERNAME_FIELD, stringSchema().with(minLength(1)))
              .requiredProperty(ACCESS_FIELD, Access.SCHEMA)
              .toJson());

  public UpdatePermissionsRequestDto(JsonObject jsonObject) {
    this(
        jsonObject.getString(USERNAME_FIELD),
        Access.fromJson(jsonObject.getJsonObject(ACCESS_FIELD)));
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(USERNAME_FIELD, username).put(ACCESS_FIELD, access.toJson());
  }

  public static Builder builder() {
    return new AutoBuilder_UpdatePermissionsRequestDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder username(String username);

    Builder access(Access access);

    UpdatePermissionsRequestDto build();
  }
}
