/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api.dto;

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
public class CheckTokenRequestDto {

  private String token;

  public CheckTokenRequestDto(JsonObject jsonObject) {
    CheckTokenRequestDtoConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CheckTokenRequestDtoConverter.toJson(this, json);
    return json;
  }
}
