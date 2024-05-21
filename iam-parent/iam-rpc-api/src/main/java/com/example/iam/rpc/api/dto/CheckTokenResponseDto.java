/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.jsonwriter.annotation.JsonWriter;
import io.vertx.core.json.JsonObject;

@JsonWriter
public record CheckTokenResponseDto(boolean valid, String userPrincipal, String userAttributes) {

  public static Builder builder() {
    return new AutoBuilder_CheckTokenResponseDto_Builder();
  }

  public static CheckTokenResponseDto fromJson(JsonObject json) {
    return CheckTokenResponseDto_JsonWriter.fromJson(json);
  }

  public JsonObject toJson() {
    return CheckTokenResponseDto_JsonWriter.toJson(this);
  }

  @AutoBuilder
  public interface Builder {

    Builder valid(boolean valid);

    Builder userPrincipal(String userPrincipal);

    Builder userAttributes(String userAttributes);

    CheckTokenResponseDto build();
  }
}
