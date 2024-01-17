/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

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
public class CheckTokenRequest {

  private String token;

  public CheckTokenRequest(JsonObject jsonObject) {
    CheckTokenRequestConverter.fromJson(jsonObject, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CheckTokenRequestConverter.toJson(this, json);
    return json;
  }
}
