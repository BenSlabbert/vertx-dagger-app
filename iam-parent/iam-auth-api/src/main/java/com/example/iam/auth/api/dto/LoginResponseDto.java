/* Licensed under Apache-2.0 2023. */
package com.example.iam.auth.api.dto;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxdaggercommons.web.serialization.JsonWriter;
import io.vertx.core.json.JsonObject;
import java.util.Objects;

public record LoginResponseDto(String token, String refreshToken) implements JsonWriter {

  public static String TOKEN_FIELD = "token";
  public static String REFRESH_TOKEN_FIELD = "refreshToken";

  public LoginResponseDto(JsonObject jsonObject) {
    this(jsonObject.getString(TOKEN_FIELD), jsonObject.getString(REFRESH_TOKEN_FIELD));
  }

  public LoginResponseDto {
    Objects.requireNonNull(token);
    Objects.requireNonNull(refreshToken);
  }

  @Override
  public JsonObject toJson() {
    return new JsonObject().put(TOKEN_FIELD, token).put(REFRESH_TOKEN_FIELD, refreshToken);
  }

  public static Builder builder() {
    return new AutoBuilder_LoginResponseDto_Builder();
  }

  @AutoBuilder
  public interface Builder {

    Builder token(String token);

    Builder refreshToken(String refreshToken);

    LoginResponseDto build();
  }
}
