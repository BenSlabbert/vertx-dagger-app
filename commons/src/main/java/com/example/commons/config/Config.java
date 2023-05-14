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
    JsonObject grpcConfig = jsonObject.getJsonObject("grpcConfig");
    JsonObject redisConfig = jsonObject.getJsonObject("redisConfig", new JsonObject());
    JsonObject postgresConfig = jsonObject.getJsonObject("postgresConfig", new JsonObject());
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    ConfigBuilder builder = Config.builder();

    if (!redisConfig.isEmpty()) {
      builder.redisConfig(
          RedisConfig.builder()
              .host(redisConfig.getString("host"))
              .port(redisConfig.getInteger("port"))
              .database(redisConfig.getInteger("database"))
              .build());
    }

    if (!postgresConfig.isEmpty()) {
      builder.postgresConfig(
          PostgresConfig.builder()
              .host(postgresConfig.getString("host"))
              .port(postgresConfig.getInteger("port"))
              .username(postgresConfig.getString("username"))
              .password(postgresConfig.getString("password"))
              .database(postgresConfig.getString("database"))
              .build());
    }

    return builder
        .httpConfig(HttpConfig.builder().port(httpConfig.getInteger("port")).build())
        .grpcConfig(GrpcConfig.builder().port(grpcConfig.getInteger("port")).build())
        .verticleConfig(
            VerticleConfig.builder()
                .numberOfInstances(verticleConfig.getInteger("numberOfInstances", 1))
                .build())
        .build();
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
