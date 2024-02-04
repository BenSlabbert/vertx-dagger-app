/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.api.dto;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.commons.web.serialization.JsonWriter;
import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Objects;

public record GetNextDeliveryJobRequestDto(String truckId) implements JsonWriter {

  public static String TRUCK_ID_FIELD = "truck_id";

  private static final JsonSchema SCHEMA =
      JsonSchema.of(
          objectSchema()
              .requiredProperty(TRUCK_ID_FIELD, stringSchema().with(minLength(1)))
              .toJson());

  public GetNextDeliveryJobRequestDto(JsonObject jsonObject) {
    this(jsonObject.getString(TRUCK_ID_FIELD));
  }

  public GetNextDeliveryJobRequestDto {
    Objects.requireNonNull(truckId);
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(TRUCK_ID_FIELD, truckId);
  }

  public static JsonSchema getSchema() {
    return SCHEMA;
  }

  public static Builder builder() {
    return new AutoBuilder_GetNextDeliveryJobRequestDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder truckId(String truckId);

    GetNextDeliveryJobRequestDto build();
  }
}
