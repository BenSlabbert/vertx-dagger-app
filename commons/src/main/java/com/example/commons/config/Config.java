/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import io.vertx.core.json.JsonObject;
import lombok.Builder;

@Builder
public record Config(
    HttpConfig httpConfig,
    RedisConfig redisConfig,
    PostgresConfig postgresConfig,
    VerticleConfig verticleConfig) {

  public static Config fromJson(JsonObject jsonObject) {
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    ConfigBuilder builder = Config.builder();
    addHttpConfig(jsonObject, builder);
    addRedisConfig(jsonObject, builder);
    addPostgresConfig(jsonObject, builder);

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

  private static boolean isNullOrEmpty(JsonObject config) {
    return null == config || config.isEmpty();
  }

  @Builder
  public record VerticleConfig(int numberOfInstances) {}

  @Builder
  public record HttpConfig(int port) {}

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
