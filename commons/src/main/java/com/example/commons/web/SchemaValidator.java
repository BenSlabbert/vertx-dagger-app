package com.example.commons.web;

import static java.util.logging.Level.WARNING;

import com.example.commons.config.Config;
import com.example.commons.web.serialization.JsonWriter;
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

  private final Map<Class<? extends JsonWriter>, JsonSchema> registry;
  private final SchemaRepository repository;

  public SchemaValidator(
      Config.HttpConfig httpConfig, Map<Class<? extends JsonWriter>, JsonSchema> registry) {
    this.registry = registry;
    this.repository =
        SchemaRepository.create(
            new JsonSchemaOptions()
                .setBaseUri("http://localhost:" + httpConfig.port())
                .setDraft(Draft.DRAFT7));

    for (JsonSchema schema : registry.values()) {
      repository.dereference(schema);
    }
  }

  public Boolean validate(Class<? extends JsonWriter> clazz, JsonObject jsonObject) {
    JsonSchema schema = registry.get(clazz);

    if (null == schema) {
      log.log(WARNING, "no schema found for: {0}", new Object[] {clazz});
      return Boolean.FALSE;
    }

    OutputUnit outputUnit = repository.validator(schema).validate(jsonObject);
    return outputUnit.getValid();
  }
}
