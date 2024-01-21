/* Licensed under Apache-2.0 2023. */
package com.example.commons.web;

import static java.util.logging.Level.WARNING;

import com.example.commons.config.Config;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaRepository;
import java.util.Map;
import lombok.extern.java.Log;

@Log
public class SchemaValidator {

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
      log.log(WARNING, "no schema found for: {0}", new Object[] {clazz});
      return Boolean.FALSE;
    }

    OutputUnit outputUnit = repository.validator(schema).validate(jsonObject);
    return outputUnit.getValid();
  }
}
