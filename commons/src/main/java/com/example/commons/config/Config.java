package com.example.commons.config;

import io.vertx.core.json.JsonObject;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record Config(
    HttpConfig httpConfig,
    GrpcConfig grpcConfig,
    RedisConfig redisConfig,
    Map<ServiceIdentifier, ServiceRegistryConfig> serviceRegistryConfig,
    VerticleConfig verticleConfig) {

  public enum ServiceIdentifier {
    IAM("iam"),
    CATALOG("catalog");

    private final String name;

    ServiceIdentifier(String name) {
      this.name = name;
    }

    static Set<String> names() {
      return Arrays.stream(ServiceIdentifier.values())
          .map(ServiceIdentifier::name)
          .collect(Collectors.toSet());
    }

    static ServiceIdentifier fromString(String in) {
      return Arrays.stream(ServiceIdentifier.values())
          .filter(f -> f.name.equals(in))
          .findFirst()
          .orElseThrow();
    }
  }

  public static Config defaults() {
    return Config.builder()
        .httpConfig(HttpConfig.builder().port(8080).build())
        .redisConfig(RedisConfig.builder().host("localhost").port(6379).database(0).build())
        .build();
  }

  public static Config fromJson(JsonObject jsonObject) {
    JsonObject httpConfig = jsonObject.getJsonObject("httpConfig");
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    ConfigBuilder builder = Config.builder();
    addRedisConfig(jsonObject, builder);
    addGrpcConfig(jsonObject, builder);
    addServiceRegistryConfig(jsonObject, builder);

    return builder
        .httpConfig(HttpConfig.builder().port(httpConfig.getInteger("port")).build())
        .verticleConfig(
            VerticleConfig.builder()
                .numberOfInstances(verticleConfig.getInteger("numberOfInstances", 1))
                .build())
        .build();
  }

  private static void addGrpcConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("grpcConfig", new JsonObject());

    if (config.isEmpty()) {
      return;
    }

    builder.grpcConfig(GrpcConfig.builder().port(config.getInteger("port")).build());
  }

  private static void addRedisConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("redisConfig", new JsonObject());

    if (config.isEmpty()) {
      return;
    }

    builder.redisConfig(
        RedisConfig.builder()
            .host(config.getString("host"))
            .port(config.getInteger("port"))
            .database(config.getInteger("database"))
            .build());
  }

  private static void addServiceRegistryConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("serviceRegistryConfig", new JsonObject());

    if (config.isEmpty()) {
      builder.serviceRegistryConfig(Map.of());
      return;
    }

    Map<ServiceIdentifier, ServiceRegistryConfig> map =
        Arrays.stream(ServiceIdentifier.values())
            .filter(serviceIdentifier -> null != config.getJsonObject(serviceIdentifier.name()))
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    serviceIdentifier -> {
                      JsonObject configJsonObject = config.getJsonObject(serviceIdentifier.name());

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
}
