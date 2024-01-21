/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.web.dto;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Objects;

public record CreatePersonRequest(String name) {

  public static String NAME_FIELD = "name";

  public CreatePersonRequest(JsonObject jsonObject) {
    this(Objects.requireNonNull(jsonObject.getString(NAME_FIELD)));
  }

  public static JsonSchema getSchema() {
    return JsonSchema.of(
        objectSchema().requiredProperty(NAME_FIELD, stringSchema().with(minLength(1))).toJson());
  }
}
