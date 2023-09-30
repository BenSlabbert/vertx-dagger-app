/* Licensed under Apache-2.0 2023. */
package com.example.catalog.web;

import static io.vertx.json.schema.common.dsl.Schemas.numberSchema;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
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
        CreateItemRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(CreateItemRequestDto.NAME_FIELD, stringSchema())
                .requiredProperty(CreateItemRequestDto.PRICE_IN_CENTS_FIELD, numberSchema())
                .toJson()));

    map.put(
        UpdateItemRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(UpdateItemRequestDto.NAME_FIELD, stringSchema())
                .requiredProperty(UpdateItemRequestDto.PRICE_IN_CENTS_FIELD, numberSchema())
                .requiredProperty(UpdateItemRequestDto.VERSION_FIELD, numberSchema())
                .toJson()));

    return map;
  }
}
