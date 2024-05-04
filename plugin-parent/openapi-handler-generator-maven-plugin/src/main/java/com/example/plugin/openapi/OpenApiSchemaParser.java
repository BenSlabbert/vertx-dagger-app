/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import com.example.plugin.openapi.type.In;
import com.example.plugin.openapi.type.Method;
import com.example.plugin.openapi.type.ParamType;
import com.example.plugin.openapi.type.SchemaType;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;

@Named
@Singleton
@SuppressWarnings("unchecked")
class OpenApiSchemaParser implements SchemaParser {

  @Override
  public List<RequestResponseSchema> parseFile(File file) throws MojoExecutionException {

    try (InputStream inputStream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
      Map<String, Object> yaml = new Yaml().load(inputStream);
      var components = (Map<String, Object>) yaml.get("components");
      var parameters = (Map<String, Object>) components.get("parameters");
      var schemas = (Map<String, Map<String, Object>>) components.get("schemas");

      Set<ObjectSchema> globalObjectSchemas = getObjectSchemas(schemas);
      Set<ParameterSchema> globalParameterSchemas =
          getParameterSchemas(Set.of(), parameters.entrySet());

      var paths = (Map<String, Object>) yaml.get("paths");

      List<RequestResponseSchema> requestResponseSchemas = new ArrayList<>();
      for (var path : paths.entrySet()) {
        String requestPath = path.getKey();
        var pathMethods = (Map<String, Object>) path.getValue();

        for (Map.Entry<String, Object> methodEntry : pathMethods.entrySet()) {
          var method = Method.fromString(methodEntry.getKey());

          var value = (Map<String, Object>) methodEntry.getValue();

          var methodParameters = (List<Map<String, Object>>) value.get("parameters");
          var methodParameterSchemas =
              getMethodParameterSchemas(globalParameterSchemas, methodParameters);

          var requestBody = (Map<String, Object>) value.get("requestBody");
          var requestBodySchema = getRequestBodySchema(globalObjectSchemas, requestBody);

          var responses = (Map<String, Object>) value.get("responses");
          var responseSchemas = getResponseSchema(globalObjectSchemas, responses);

          requestResponseSchemas.add(
              new RequestResponseSchema(
                  requestPath, method, methodParameterSchemas, requestBodySchema, responseSchemas));
        }
      }

      return requestResponseSchemas;
    } catch (MojoExecutionException e) {
      throw e;
    } catch (Exception e) {
      throw new MojoExecutionException("Error while reading file", e);
    }
  }

  private static ResponseSchema getResponseSchema(
      Set<ObjectSchema> globalObjectSchemas, Map<String, Object> responses) {

    for (Map.Entry<String, Object> entry : responses.entrySet()) {
      String statusCode = entry.getKey();
      var value = (Map<String, Object>) entry.getValue();
      var content = (Map<String, Object>) value.get("content");
      if (null == content) {
        return new ResponseSchema(Integer.parseInt(statusCode), true, null, null);
      }

      var applicationJsonContent = (Map<String, Object>) content.get("application/json");
      var schema = (Map<String, Object>) applicationJsonContent.get("schema");

      var type = (String) schema.get("type");
      SchemaType schemaType = null == type ? SchemaType.OBJECT : SchemaType.fromString(type);
      if (SchemaType.ARRAY == schemaType) {
        var items = (Map<String, Object>) schema.get("items");
        var ref = (String) items.get("$ref");
        String[] split = ref.split("/");
        String id = split[split.length - 1];
        Optional<ObjectSchema> maybeSchema =
            globalObjectSchemas.stream().filter(f -> f.name().equals(id)).findFirst();
        if (maybeSchema.isEmpty()) {
          throw new IllegalArgumentException("Could not find schema: " + ref);
        }

        return new ResponseSchema(
            Integer.parseInt(statusCode), false, schemaType, maybeSchema.get());
      }

      // object schema
      var ref = (String) schema.get("$ref");
      String[] split = ref.split("/");
      String id = split[split.length - 1];
      Optional<ObjectSchema> maybeSchema =
          globalObjectSchemas.stream().filter(f -> f.name().equals(id)).findFirst();
      if (maybeSchema.isEmpty()) {
        throw new IllegalArgumentException("Could not find schema: " + ref);
      }

      return new ResponseSchema(Integer.parseInt(statusCode), false, schemaType, maybeSchema.get());
    }

    throw new IllegalArgumentException("No response schema found");
  }

  private static Optional<RequestBodySchema> getRequestBodySchema(
      Set<ObjectSchema> globalObjectSchemas, Map<String, Object> requestBody) {
    if (null == requestBody) return Optional.empty();

    var required = toBool((Boolean) requestBody.get("required"));
    var content = (Map<String, Object>) requestBody.get("content");
    var applicationJsonContent = (Map<String, Object>) content.get("application/json");
    var schema = (Map<String, Object>) applicationJsonContent.get("schema");

    if (schema.containsKey("$ref")) {
      var ref = (String) schema.get("$ref");
      String[] split = ref.split("/");
      String id = split[split.length - 1];
      Optional<ObjectSchema> maybeSchema =
          globalObjectSchemas.stream().filter(f -> f.name().equals(id)).findFirst();

      if (maybeSchema.isEmpty()) {
        throw new IllegalArgumentException("Could not find schema: " + ref);
      }
      return Optional.of(new RequestBodySchema(required, maybeSchema.get()));
    }

    return Optional.empty();
  }

  private static List<ParameterSchema> getMethodParameterSchemas(
      Set<ParameterSchema> globalParameterSchemas, List<Map<String, Object>> methodParameters) {

    if (null == methodParameters) return List.of();

    List<ParameterSchema> methodParameterSchemas = new ArrayList<>();

    for (var methodParameter : methodParameters) {
      ParameterSchema methodParameterSchema =
          getParameterSchemas(globalParameterSchemas, methodParameter);
      methodParameterSchemas.add(methodParameterSchema);
    }

    return methodParameterSchemas;
  }

  private static Set<ObjectSchema> getObjectSchemas(Map<String, Map<String, Object>> schemas)
      throws MojoExecutionException {
    Set<ObjectSchema> objectSchemas = new HashSet<>();
    for (Map.Entry<String, Map<String, Object>> schema : schemas.entrySet()) {
      var objectName = schema.getKey();
      var type = (String) schema.getValue().get("type");
      if (!"object".equals(type)) {
        throw new MojoExecutionException("Only object type is supported");
      }

      var properties = (Map<String, Object>) schema.getValue().get("properties");

      List<ObjectSchema.Property> propertiesList = new ArrayList<>();
      for (var propertyEntry : properties.entrySet()) {
        var propertyName = propertyEntry.getKey();
        var value = (Map<String, Object>) propertyEntry.getValue();
        var propertyType = (String) value.get("type");
        var required = (Boolean) value.get("required");
        var paramType = ParamType.fromString(propertyType);
        propertiesList.add(new ObjectSchema.Property(propertyName, paramType, toBool(required)));
      }

      ObjectSchema objectSchema = new ObjectSchema(objectName, propertiesList);
      objectSchemas.add(objectSchema);
    }
    return objectSchemas;
  }

  private static boolean toBool(Boolean bool) {
    if (null == bool) return false;

    return bool;
  }

  private static Set<ParameterSchema> getParameterSchemas(
      Set<ParameterSchema> globalParameterSchemas, Set<Map.Entry<String, Object>> entrySet) {
    Set<ParameterSchema> parameterSchemas = new HashSet<>();
    for (var parameterEntry : entrySet) {
      var value = (Map<String, Object>) parameterEntry.getValue();
      parameterSchemas.add(getParameterSchemas(globalParameterSchemas, value));
    }
    return parameterSchemas;
  }

  private static ParameterSchema getParameterSchemas(
      Set<ParameterSchema> globalParameterSchemas, Map<String, Object> value) {

    if (value.containsKey("$ref")) {
      // need to look this up from the components
      var ref = (String) value.get("$ref");
      String[] split = ref.split("/");
      String id = split[split.length - 1];
      Optional<ParameterSchema> maybeSchema =
          globalParameterSchemas.stream().filter(f -> f.name().equals(id)).findFirst();

      if (maybeSchema.isEmpty()) {
        throw new IllegalArgumentException("Could not find schema: " + ref);
      }

      return maybeSchema.get();
    }

    var paramName = (String) value.get("name");
    var in = (String) value.get("in");
    var required = (Boolean) value.get("required");
    var schema = (Map<String, Object>) value.get("schema");
    var type = (String) schema.get("type");
    var paramType = ParamType.fromString(type);
    return new ParameterSchema(paramName, paramType, toBool(required), In.fromString(in));
  }
}
