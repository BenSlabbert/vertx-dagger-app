/* Licensed under Apache-2.0 2023. */
package com.example.warehouse.web;

import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SchemaValidatorDelegator {

  private final SchemaValidator schemaValidator;

  @Inject
  SchemaValidatorDelegator(Config.HttpConfig httpConfig) {
    this.schemaValidator = new SchemaValidator(httpConfig, getRegistry());
  }

  public Boolean validate(Class<?> clazz, JsonObject jsonObject) {
    return schemaValidator.validate(clazz, jsonObject);
  }

  private Map<Class<?>, JsonSchema> getRegistry() {
    return Map.ofEntries(
        // todo we can probably delete this since we will use a vertx event
        //        Map.entry(GetNextDeliveryJobRequestDto.class,
        // GetNextDeliveryJobRequestDto.getSchema())
        );
  }
}
