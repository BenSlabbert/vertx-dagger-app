package com.example.commons.config;

import io.vertx.core.json.JsonObject;
import lombok.Builder;

@Builder
public record Config(
    HttpConfig httpConfig,
    GrpcConfig grpcConfig,
    RedisConfig redisConfig,
    PostgresConfig postgresConfig,
    VerticleConfig verticleConfig) {

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
    addPostgresConfig(jsonObject, builder);
    addGrpcConfig(jsonObject, builder);

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

  private static void addPostgresConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("postgresConfig", new JsonObject());

    if (config.isEmpty()) {
      return;
    }

    builder.postgresConfig(
        PostgresConfig.builder()
            .host(config.getString("host"))
            .port(config.getInteger("port"))
            .username(config.getString("username"))
            .password(config.getString("password"))
            .database(config.getString("database"))
            .build());
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

  @Builder
  public record VerticleConfig(int numberOfInstances) {}

  @Builder
  public record HttpConfig(int port) {}

  @Builder
  public record GrpcConfig(int port) {}

  @Builder
  public record RedisConfig(String host, int port, int database) {

    public String uri() {
      return String.format("redis://%s:%d/%d", host, port, database);
    }
  }

  @Builder
  public record PostgresConfig(
      String host, int port, String database, String username, String password) {}
}
