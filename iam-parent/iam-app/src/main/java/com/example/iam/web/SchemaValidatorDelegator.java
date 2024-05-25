/* Licensed under Apache-2.0 2023. */
package com.example.iam.web;

import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.LoginRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RefreshRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.RegisterRequestDto;
import github.benslabbert.vertxdaggerapp.api.iam.auth.dto.UpdatePermissionsRequestDto;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.web.SchemaValidator;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SchemaValidatorDelegator {

  private static final Logger log = LoggerFactory.getLogger(SchemaValidatorDelegator.class);

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
        Map.entry(LoginRequestDto.class, LoginRequestDto.SCHEMA),
        Map.entry(RefreshRequestDto.class, RefreshRequestDto.SCHEMA),
        Map.entry(RegisterRequestDto.class, RegisterRequestDto.SCHEMA),
        Map.entry(UpdatePermissionsRequestDto.class, UpdatePermissionsRequestDto.SCHEMA));
  }
}
