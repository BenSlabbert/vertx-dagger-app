package com.example.catalog.web;

import static io.vertx.json.schema.common.dsl.Schemas.numberSchema;
import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;

import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
import com.example.commons.web.serialization.JsonWriter;
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
        CreateItemRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(CreateItemRequestDto.NAME_FIELD, stringSchema())
                .requiredProperty(CreateItemRequestDto.PRICE_IN_CENTS_FIELD, numberSchema())
                .toJson()));

    return map;
  }
}
