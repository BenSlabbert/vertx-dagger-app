/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import com.google.auto.value.AutoBuilder;
import io.vertx.core.json.JsonObject;
import java.time.Duration;
import javax.annotation.Nullable;

public record Config(
    Profile profile,
    @Nullable HttpConfig httpConfig,
    @Nullable RedisConfig redisConfig,
    @Nullable PostgresConfig postgresConfig,
    @Nullable JdbcConfig jdbcConfig) {

  public static Builder builder() {
    return new AutoBuilder_Config_Builder().profile(Profile.PROD);
  }

  @AutoBuilder
  public interface Builder {
    Builder profile(Profile profile);

    Builder httpConfig(@Nullable HttpConfig httpConfig);

    Builder redisConfig(@Nullable RedisConfig redisConfig);

    Builder postgresConfig(@Nullable PostgresConfig postgresConfig);

    Builder jdbcConfig(@Nullable JdbcConfig jdbcConfig);

    Config build();
  }

  public static Config fromJson(JsonObject jsonObject) {
    Builder builder = Config.builder();

    addProfile(jsonObject, builder);
    addHttpConfig(jsonObject, builder);
    addRedisConfig(jsonObject, builder);
    addPostgresConfig(jsonObject, builder);
    addJdbcConfig(jsonObject, builder);

    return builder.build();
  }

  private static void addProfile(JsonObject jsonObject, Builder builder) {
    String profile = jsonObject.getString("profile", null);
    builder.profile(Profile.fromString(profile));
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

  private static void addJdbcConfig(JsonObject jsonObject, Builder builder) {
    JsonObject config = jsonObject.getJsonObject("jdbcConfig", new JsonObject());

    if (isNullOrEmpty(config)) {
      return;
    }

    builder.jdbcConfig(
        JdbcConfig.builder()
            .fetchSize(config.getInteger("fetchSize"))
            .queryTimeout(Duration.ofSeconds(config.getInteger("queryTimeout")))
            .build());
  }

  private static boolean isNullOrEmpty(JsonObject config) {
    return null == config || config.isEmpty();
  }

  public enum Profile {
    DEV,
    PROD;

    static Profile fromString(String value) {
      return switch (value) {
        case "dev" -> DEV;
        case "prod" -> PROD;
        case null -> PROD;
        default -> throw new IllegalArgumentException("Invalid profile: " + value);
      };
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

  public record JdbcConfig(int fetchSize, Duration queryTimeout) {

    public static Builder builder() {
      return new AutoBuilder_Config_JdbcConfig_Builder();
    }

    @AutoBuilder
    public interface Builder {
      Builder fetchSize(int fetchSize);

      Builder queryTimeout(Duration queryTimeout);

      JdbcConfig build();
    }
  }
}
