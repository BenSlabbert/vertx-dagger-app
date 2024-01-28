/* Licensed under Apache-2.0 2023. */
package com.example.commons.web;

import com.example.commons.config.Config;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaRepository;
import java.util.Map;

public class SchemaValidator {

  private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);

  private final Map<Class<?>, JsonSchema> registry;
  private final SchemaRepository repository;

  public SchemaValidator(Config.HttpConfig httpConfig, Map<Class<?>, JsonSchema> registry) {
    this.registry = registry;
    this.repository =
        SchemaRepository.create(
            new JsonSchemaOptions()
                .setBaseUri("http://127.0.0.1:" + httpConfig.port())
                .setDraft(Draft.DRAFT7));

    registry.values().forEach(repository::dereference);
  }

  public Boolean validate(Class<?> clazz, JsonObject jsonObject) {
    JsonSchema schema = registry.get(clazz);

    if (null == schema) {
      log.warn("no schema found for: " + clazz);
      return Boolean.FALSE;
    }

    OutputUnit outputUnit = repository.validator(schema).validate(jsonObject);
    return outputUnit.getValid();
  }
}
