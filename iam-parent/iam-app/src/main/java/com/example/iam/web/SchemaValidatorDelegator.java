/* Licensed under Apache-2.0 2023. */
package com.example.iam.web;

import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
import com.example.iam.auth.api.dto.LoginRequestDto;
import com.example.iam.auth.api.dto.RefreshRequestDto;
import com.example.iam.auth.api.dto.RegisterRequestDto;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class SchemaValidatorDelegator {

  private final SchemaValidator schemaValidator;

  @Inject
  public SchemaValidatorDelegator(Config.HttpConfig httpConfig) {
    this.schemaValidator = new SchemaValidator(httpConfig, getRegistry());
  }

  public Boolean validate(Class<?> clazz, JsonObject jsonObject) {
    return schemaValidator.validate(clazz, jsonObject);
  }

  private Map<Class<?>, JsonSchema> getRegistry() {
    return Map.ofEntries(
        Map.entry(LoginRequestDto.class, LoginRequestDto.getSchema()),
        Map.entry(RefreshRequestDto.class, RefreshRequestDto.getSchema()),
        Map.entry(RegisterRequestDto.class, RegisterRequestDto.getSchema()));
  }
}
