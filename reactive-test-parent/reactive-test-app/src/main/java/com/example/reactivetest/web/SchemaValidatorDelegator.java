/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.web;

import com.example.commons.config.Config;
import com.example.commons.web.SchemaValidator;
import com.example.reactivetest.web.dto.CreatePersonRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.JsonSchema;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SchemaValidatorDelegator {

  private final SchemaValidator schemaValidator;

  @Inject
  public SchemaValidatorDelegator(Config config) {
    this.schemaValidator = new SchemaValidator(config.httpConfig(), getRegistry());
  }

  public Boolean validate(Class<?> clazz, JsonObject jsonObject) {
    return schemaValidator.validate(clazz, jsonObject);
  }

  private Map<Class<?>, JsonSchema> getRegistry() {
    Map<Class<?>, JsonSchema> map = new HashMap<>();
    map.put(CreatePersonRequest.class, CreatePersonRequest.getSchema());
    return map;
  }
}
