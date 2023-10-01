/* Licensed under Apache-2.0 2023. */
package com.example.iam.web;

import static io.vertx.json.schema.common.dsl.Keywords.minLength;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.HashMap;
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
    Map<Class<?>, JsonSchema> map = new HashMap<>();

    map.put(
        LoginRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(LoginRequestDto.USERNAME_FIELD, stringSchema().with(minLength(1)))
                .requiredProperty(LoginRequestDto.PASSWORD_FIELD, stringSchema().with(minLength(1)))
                .toJson()));

    map.put(
        RefreshRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(
                    RefreshRequestDto.USERNAME_FIELD, stringSchema().with(minLength(1)))
                .requiredProperty(RefreshRequestDto.TOKEN_FIELD, stringSchema().with(minLength(1)))
                .toJson()));

    map.put(
        RegisterRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(
                    RegisterRequestDto.USERNAME_FIELD, stringSchema().with(minLength(1)))
                .requiredProperty(
                    RegisterRequestDto.PASSWORD_FIELD, stringSchema().with(minLength(1)))
                .toJson()));

    return map;
  }
}
