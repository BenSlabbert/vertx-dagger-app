package com.example.iam.web;

import static io.vertx.json.schema.common.dsl.Schemas.objectSchema;
import static io.vertx.json.schema.common.dsl.Schemas.stringSchema;
import static java.util.logging.Level.WARNING;

import com.example.commons.config.Config;
import com.example.iam.web.route.dto.JsonWriter;
import com.example.iam.web.route.dto.LoginRequestDto;
import com.example.iam.web.route.dto.RefreshRequestDto;
import com.example.iam.web.route.dto.RegisterRequestDto;
import io.vertx.core.json.JsonObject;
import io.vertx.json.schema.Draft;
import io.vertx.json.schema.JsonSchema;
import io.vertx.json.schema.JsonSchemaOptions;
import io.vertx.json.schema.OutputUnit;
import io.vertx.json.schema.SchemaRepository;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class SchemaValidator {

  private static final Map<Class<? extends JsonWriter>, JsonSchema> REGISTRY = new HashMap<>();

  static {
    REGISTRY.put(
        LoginRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(LoginRequestDto.USERNAME_FIELD, stringSchema())
                .requiredProperty(LoginRequestDto.PASSWORD_FIELD, stringSchema())
                .toJson()));

    REGISTRY.put(
        RefreshRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(RefreshRequestDto.USERNAME_FIELD, stringSchema())
                .requiredProperty(RefreshRequestDto.TOKEN_FIELD, stringSchema())
                .toJson()));

    REGISTRY.put(
        RegisterRequestDto.class,
        JsonSchema.of(
            objectSchema()
                .requiredProperty(RegisterRequestDto.USERNAME_FIELD, stringSchema())
                .requiredProperty(RegisterRequestDto.PASSWORD_FIELD, stringSchema())
                .toJson()));
  }

  private final SchemaRepository repository;

  @Inject
  public SchemaValidator(Config.HttpConfig httpConfig) {
    this.repository =
        SchemaRepository.create(
            new JsonSchemaOptions()
                .setBaseUri("http://localhost:" + httpConfig.port())
                .setDraft(Draft.DRAFT7));

    for (JsonSchema schema : REGISTRY.values()) {
      repository.dereference(schema);
    }
  }

  public Boolean validate(Class<? extends JsonWriter> clazz, JsonObject jsonObject) {
    JsonSchema schema = REGISTRY.get(clazz);

    if (null == schema) {
      log.log(WARNING, "no schema found for: {0}", new Object[] {clazz});
      return Boolean.FALSE;
    }

    OutputUnit outputUnit = repository.validator(schema).validate(jsonObject);
    return outputUnit.getValid();
  }
}
