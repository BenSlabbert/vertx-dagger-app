/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Builder
public record Config(
    HttpConfig httpConfig,
    GrpcConfig grpcConfig,
    RedisConfig redisConfig,
    PostgresConfig postgresConfig,
    Map<ServiceIdentifier, ServiceRegistryConfig> serviceRegistryConfig,
    VerticleConfig verticleConfig) {

  /**
   * <strong>NOTE:</strong> only use this for test!
   *
   * <p>this depends on jackson and reflection which we do not want in the running application
   */
  public JsonObject encode() {
    String encode = new JsonObject().put("empty", this).encode();
    return new JsonObject(encode).getJsonObject("empty");
  }

  @Getter
  public enum ServiceIdentifier {
    IAM("IAM"),
    CATALOG("CATALOG");

    private final String serviceName;

    ServiceIdentifier(String serviceName) {
      this.serviceName = serviceName;
    }

    static Set<String> names() {
      return Arrays.stream(ServiceIdentifier.values())
          .map(ServiceIdentifier::name)
          .collect(Collectors.toSet());
    }

    static ServiceIdentifier fromString(String in) {
      return Arrays.stream(ServiceIdentifier.values())
          .filter(f -> f.serviceName.equals(in))
          .findFirst()
          .orElseThrow();
    }
  }

  static Config defaults() {
    return Config.builder()
        .httpConfig(HttpConfig.builder().port(8080).build())
        .redisConfig(RedisConfig.builder().host("127.0.0.1").port(6379).database(0).build())
        .build();
  }

  static Config fromJson(JsonObject jsonObject) {
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    ConfigBuilder builder = Config.builder();
    addHttpConfig(jsonObject, builder);
    addRedisConfig(jsonObject, builder);
    addPostgresConfig(jsonObject, builder);
    addGrpcConfig(jsonObject, builder);
    addServiceRegistryConfig(jsonObject, builder);

    return builder
        .verticleConfig(
            VerticleConfig.builder()
                .numberOfInstances(verticleConfig.getInteger("numberOfInstances", 1))
                .build())
        .build();
  }

  private static void addHttpConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("httpConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      return;
    }

    HttpConfig httpConfig = HttpConfig.builder().port(config.getInteger("port")).build();
    builder.httpConfig(httpConfig);
  }


  private static void addGrpcConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("grpcConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      return;
    }

    GrpcConfig grpcConfig = GrpcConfig.builder().port(config.getInteger("port")).build();
    builder.grpcConfig(grpcConfig);
  }

  private static void addRedisConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("redisConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      return;
    }

    builder.redisConfig(
        RedisConfig.builder()
            .host(config.getString("host"))
            .port(config.getInteger("port"))
            .database(config.getInteger("database"))
            .build());
  }

  private static void addPostgresConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("postgresConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      return;
    }

    builder.postgresConfig(
        PostgresConfig.builder()
            .host(config.getString("host"))
            .port(config.getInteger("port"))
            .database(config.getString("database"))
            .password(config.getString("password"))
            .username(config.getString("username"))
            .build());
  }

  private static void addServiceRegistryConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("serviceRegistryConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      builder.serviceRegistryConfig(Map.of());
      return;
    }
    Map<ServiceIdentifier, ServiceRegistryConfig> map =
        Arrays.stream(ServiceIdentifier.values())
            .filter(
                serviceIdentifier -> null != config.getJsonObject(serviceIdentifier.serviceName()))
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    serviceIdentifier -> {
                      JsonObject configJsonObject =
                          config.getJsonObject(serviceIdentifier.serviceName());

                      return ServiceRegistryConfig.builder()
                          .protocol(
                              ServiceRegistryConfig.Protocol.fromString(
                                  configJsonObject.getString("protocol")))
                          .host(configJsonObject.getString("host"))
                          .port(configJsonObject.getInteger("port"))
                          .build();
                    }));

    builder.serviceRegistryConfig(map);
  }

  private static boolean isNullOrEmpty(JsonObject config) {
    return null == config || config.isEmpty();
  }

  @Builder
  public record VerticleConfig(int numberOfInstances) {}

  @Builder
  public record HttpConfig(int port) {}

  @Builder
  public record GrpcConfig(int port) {}

  @Builder
  public record ServiceRegistryConfig(Protocol protocol, String host, int port) {

    public enum Protocol {
      GRPC("GRPC"),
      REST("REST");

      private final String name;

      Protocol(String name) {
        this.name = name;
      }

      static Protocol fromString(String in) {
        return Arrays.stream(Protocol.values())
            .filter(f -> f.name.equals(in))
            .findFirst()
            .orElseThrow();
      }
    }
  }

  @Builder
  public record RedisConfig(String host, int port, int database) {

    public String uri() {
      return String.format("redis://%s:%d/%d", host, port, database);
    }
  }

  @Builder
  public record PostgresConfig(
      String host, int port, String username, String password, String database) {}
}
