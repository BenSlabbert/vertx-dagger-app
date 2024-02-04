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

public record GetNextDeliveryJobResponseDto(String jobId) implements JsonWriter {

  public static String JOB_ID_FIELD = "job_id";

  private static final JsonSchema SCHEMA =
      JsonSchema.of(
          objectSchema()
              .requiredProperty(JOB_ID_FIELD, stringSchema().with(minLength(1)))
              .toJson());

  public GetNextDeliveryJobResponseDto(JsonObject jsonObject) {
    this(jsonObject.getString(JOB_ID_FIELD));
  }

  public GetNextDeliveryJobResponseDto {
    Objects.requireNonNull(JOB_ID_FIELD);
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(JOB_ID_FIELD, jobId);
  }

  public static JsonSchema getSchema() {
    return SCHEMA;
  }

  public static Builder builder() {
    return new AutoBuilder_GetNextDeliveryJobResponseDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder jobId(String jobId);

    GetNextDeliveryJobResponseDto build();
  }
}
