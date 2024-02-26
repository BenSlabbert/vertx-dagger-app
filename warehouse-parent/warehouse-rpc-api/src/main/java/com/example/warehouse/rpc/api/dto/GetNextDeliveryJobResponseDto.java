/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api.dto;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// vertx codegen annotations
@JsonGen
@DataObject
// lombok annotations
@Data
@Builder
@AllArgsConstructor
public class GetNextDeliveryJobResponseDto {

  private Long deliveryId;

  public GetNextDeliveryJobResponseDto(JsonObject jsonObject) {
    GetNextDeliveryJobResponseDtoConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GetNextDeliveryJobResponseDtoConverter.toJson(this, json);
    return json;
  }
}
