/* Licensed under Apache-2.0 2023. */
package com.example.commons.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.json.JsonObject;
import java.net.URL;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ParseConfigTest {

  @Test
  void test() {
    URL resource = ParseConfigTest.class.getClassLoader().getResource("config.json");
    assertThat(resource).isNotNull();
    Config config =
        ParseConfig.parseArgs(new String[] {"-Xd=123", "-Dabc=sdf", resource.getPath()});

    Config config1 =
        new JsonObject(
                """
                        {
                          "httpConfig": {
                            "port": 8080
                          },
                          "grpcConfig": {
                            "port": 50051
                          },
                          "redisConfig": {
                            "host": "redis",
                            "port": 6379,
                            "database": 0
                          },
                          "verticleConfig": {
                            "numberOfInstances": 1
                          },
                          "serviceRegistryConfig": {
                            "IAM": {
                              "protocol": "GRPC",
                              "host": "iam",
                              "port":  50051
                            }
                          }
                        }
                        """)
            .mapTo(Config.class);

    assertThat(config)
        .isNotNull()
        .usingRecursiveComparison()
        .isEqualTo(
            Config.builder()
                .httpConfig(Config.HttpConfig.builder().port(8080).build())
                .redisConfig(
                    Config.RedisConfig.builder().host("redis").port(6379).database(0).build())
                .verticleConfig(Config.VerticleConfig.builder().numberOfInstances(1).build())
                .grpcConfig(Config.GrpcConfig.builder().port(50051).build())
                .serviceRegistryConfig(
                    Map.of(
                        Config.ServiceIdentifier.IAM,
                        Config.ServiceRegistryConfig.builder()
                            .protocol(Config.ServiceRegistryConfig.Protocol.GRPC)
                            .host("iam")
                            .port(50051)
                            .build()))
                .build());

    assertThat(config).usingRecursiveComparison().isEqualTo(config1);
  }
}
