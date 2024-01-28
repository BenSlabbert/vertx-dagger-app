/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;
import javax.annotation.Nullable;

public record Config(
    @Nullable HttpConfig httpConfig,
    @Nullable RedisConfig redisConfig,
    @Nullable PostgresConfig postgresConfig,
    @Nullable VerticleConfig verticleConfig) {

  public static Builder builder() {
    return new AutoBuilder_Config_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder httpConfig(@Nullable HttpConfig httpConfig);

    Builder redisConfig(@Nullable RedisConfig redisConfig);

    Builder postgresConfig(@Nullable PostgresConfig postgresConfig);

    Builder verticleConfig(@Nullable VerticleConfig verticleConfig);

    Config build();
  }

  public static Config fromJson(JsonObject jsonObject) {
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    Builder builder = Config.builder();
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

  private static void addHttpConfig(JsonObject jsonObject, Builder builder) {
    JsonObject config = jsonObject.getJsonObject("httpConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      return;
    }

    HttpConfig httpConfig = HttpConfig.builder().port(config.getInteger("port")).build();
    builder.httpConfig(httpConfig);
  }

  private static void addRedisConfig(JsonObject jsonObject, Builder builder) {
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

  private static void addPostgresConfig(JsonObject jsonObject, Builder builder) {
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

  public record VerticleConfig(int numberOfInstances) {

    public static Builder builder() {
      return new AutoBuilder_Config_VerticleConfig_Builder();
    }

    @AutoBuilder
    public interface Builder {
      Builder numberOfInstances(int numberOfInstances);

      VerticleConfig build();
    }
  }

  public record HttpConfig(int port) {

    public static Builder builder() {
      return new AutoBuilder_Config_HttpConfig_Builder();
    }

    @AutoBuilder
    public interface Builder {
      Builder port(int port);

      HttpConfig build();
    }
  }

  public record RedisConfig(String host, int port, int database) {

    public String uri() {
      return String.format("redis://%s:%d/%d", host, port, database);
    }

    public static Builder builder() {
      return new AutoBuilder_Config_RedisConfig_Builder();
    }

    @AutoBuilder
    public interface Builder {
      Builder host(String host);

      Builder port(int port);

      Builder database(int database);

      RedisConfig build();
    }
  }

  public record PostgresConfig(
      String host, int port, String username, String password, String database) {

    public static Builder builder() {
      return new AutoBuilder_Config_PostgresConfig_Builder();
    }

    @AutoBuilder
    public interface Builder {
      Builder host(String host);

      Builder port(int port);

      Builder username(String username);

      Builder password(String password);

      Builder database(String database);

      PostgresConfig build();
    }
  }
}
