package com.example.iam.web;

import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
import com.example.commons.web.serialization.JsonWriter;
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

  public Boolean validate(Class<? extends JsonWriter> clazz, JsonObject jsonObject) {
    return schemaValidator.validate(clazz, jsonObject);
  }

  private Map<Class<? extends JsonWriter>, JsonSchema> getRegistry() {
    Map<Class<? extends JsonWriter>, JsonSchema> map = new HashMap<>();

    map.put(
        LoginRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(LoginRequestDto.USERNAME_FIELD, stringSchema())
                .requiredProperty(LoginRequestDto.PASSWORD_FIELD, stringSchema())
                .toJson()));

    map.put(
        RefreshRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(RefreshRequestDto.USERNAME_FIELD, stringSchema())
                .requiredProperty(RefreshRequestDto.TOKEN_FIELD, stringSchema())
                .toJson()));

    map.put(
        RegisterRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(RegisterRequestDto.USERNAME_FIELD, stringSchema())
                .requiredProperty(RegisterRequestDto.PASSWORD_FIELD, stringSchema())
                .toJson()));

    return map;
  }
}
