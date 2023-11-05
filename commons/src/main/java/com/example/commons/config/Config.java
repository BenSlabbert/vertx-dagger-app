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
    KafkaConfig kafkaConfig,
    Map<ServiceIdentifier, ServiceRegistryConfig> serviceRegistryConfig,
    VerticleConfig verticleConfig) {

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
    JsonObject httpConfig = jsonObject.getJsonObject("httpConfig");
    JsonObject verticleConfig = jsonObject.getJsonObject("verticleConfig", new JsonObject());

    ConfigBuilder builder = Config.builder();
    addRedisConfig(jsonObject, builder);
    addPostgresConfig(jsonObject, builder);
    addGrpcConfig(jsonObject, builder);
    addKafkaConfig(jsonObject, builder);
    addServiceRegistryConfig(jsonObject, builder);

    return builder
        .httpConfig(HttpConfig.builder().port(httpConfig.getInteger("port")).build())
        .verticleConfig(
            VerticleConfig.builder()
                .numberOfInstances(verticleConfig.getInteger("numberOfInstances", 1))
                .build())
        .build();
  }

  private static void addKafkaConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("kafkaConfig", new JsonObject());

    if (config.isEmpty()) {
      return;
    }

    var consumer = config.getJsonObject("consumer", new JsonObject());
    KafkaConsumerConfig consumerConfig =
        KafkaConsumerConfig.builder()
            .clientId(consumer.getString("clientId"))
            .consumerGroup(consumer.getString("consumerGroup"))
            .maxPollRecords(consumer.getInteger("maxPollRecords", 1))
            .build();

    var producer = config.getJsonObject("producer", new JsonObject());
    KafkaProducerConfig producerConfig =
        KafkaProducerConfig.builder().clientId(producer.getString("clientId")).build();

    KafkaConfig kafkaConfig =
        KafkaConfig.builder()
            .bootstrapServers(config.getString("bootstrapServers"))
            .consumer(consumerConfig)
            .producer(producerConfig)
            .build();

    builder.kafkaConfig(kafkaConfig);
  }

  private static void addGrpcConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("grpcConfig", new JsonObject());

    if (config.isEmpty()) {
      return;
    }

    GrpcConfig grpcConfig = GrpcConfig.builder().port(config.getInteger("port")).build();
    builder.grpcConfig(grpcConfig);
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

  private static void addPostgresConfig(JsonObject jsonObject, ConfigBuilder builder) {
    JsonObject config = jsonObject.getJsonObject("postgresConfig", new JsonObject());

    if (config.isEmpty()) {
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

    if (config.isEmpty()) {
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
  public record KafkaConfig(
      String bootstrapServers, KafkaConsumerConfig consumer, KafkaProducerConfig producer) {}

  @Builder
  public record KafkaConsumerConfig(String clientId, String consumerGroup, int maxPollRecords) {}

  @Builder
  public record KafkaProducerConfig(String clientId) {}

  @Builder
  public record PostgresConfig(
      String host, int port, String username, String password, String database) {}
}
