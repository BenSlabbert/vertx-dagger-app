/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import static com.example.commons.FreePortUtility.getPort;
import static org.assertj.core.api.Assertions.fail;

import com.example.commons.TestcontainerLogConsumer;
import com.example.commons.config.Config;
import com.example.commons.transaction.blocking.TransactionBoundary;
import com.example.migration.FlywayProvider;
import com.example.payment.ioc.DaggerTestPersistenceProvider;
import com.example.payment.ioc.TestPersistenceProvider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import lombok.extern.java.Log;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@Log
@ExtendWith(VertxExtension.class)
public abstract class PersistenceTest {

  protected static final int HTTP_PORT = getPort();
  protected static final int GRPC_PORT = getPort();

  protected TestPersistenceProvider provider;
  protected Config config;

  private static final AtomicInteger counter = new AtomicInteger(0);
  private static final Network network = Network.newNetwork();

  protected static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.1"))
          .withNetwork(network)
          .withEnv("KAFKA_HEAP_OPTS", "-Xmx512M -Xms512M")
          .withLogConsumer(new TestcontainerLogConsumer("kafka"));

  protected static final GenericContainer<?> postgres =
      new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withExposedPorts(5432)
          .withNetwork(network)
          .withTmpFs(Map.of("/var/lib/postgresql/data", "rw,noexec,nosuid,size=100m"))
          .withNetworkAliases("postgres")
          .withEnv("POSTGRES_USER", "postgres")
          .withEnv("POSTGRES_PASSWORD", "postgres")
          .withEnv("POSTGRES_DB", "postgres")
          // must wait twice as the init process also prints this message
          .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
          .withLogConsumer(new TestcontainerLogConsumer("postgres"));

  // https://testcontainers.com/guides/testcontainers-container-lifecycle/#_using_singleton_containers
  static {
    log.info("starting kafka");
    log.info("starting postgres");
    Startables.deepStart(kafka, postgres).join();
    log.info("container startup done");
  }

  @BeforeEach
  void prepare(Vertx vertx) throws Exception {
    final String dbName = "testing" + counter.incrementAndGet();
    log.info("creating db: " + dbName);

    // create a new database for each test
    Container.ExecResult execResult =
        postgres.execInContainer("psql", "-U", "postgres", "-c", "CREATE DATABASE " + dbName);
    if (execResult.getExitCode() != 0) {
      fail("failed to create database: " + execResult.getStderr());
      return;
    }

    Flyway flyway =
        FlywayProvider.get(
            "127.0.0.1", postgres.getMappedPort(5432), "postgres", "postgres", dbName);
    flyway.clean();
    flyway.migrate();

    config =
        new Config(
            new Config.HttpConfig(HTTP_PORT),
            new Config.GrpcConfig(GRPC_PORT),
            Config.RedisConfig.builder().build(),
            new Config.PostgresConfig(
                "127.0.0.1", postgres.getMappedPort(5432), "postgres", "postgres", dbName),
            Config.KafkaConfig.builder()
                .bootstrapServers(kafka.getBootstrapServers())
                .producer(
                    Config.KafkaProducerConfig.builder()
                        .clientId("producer-id-" + counter.get())
                        .build())
                .consumer(
                    Config.KafkaConsumerConfig.builder()
                        .clientId("consumer-id-" + counter.get())
                        .consumerGroup("consumer-group-" + counter.get())
                        .maxPollRecords(1)
                        .build())
                .build(),
            Map.of(),
            new Config.VerticleConfig(1));

    Properties properties = new Properties();
    properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
    try (var adminClient = KafkaAdminClient.create(properties)) {
      adminClient.createTopics(List.of(new NewTopic("Saga.Catalog.CreatePayment", 1, (short) 1)));
    }

    provider =
        DaggerTestPersistenceProvider.builder()
            .vertx(vertx)
            .config(config)
            .httpConfig(config.httpConfig())
            .verticleConfig(config.verticleConfig())
            .serviceRegistryConfig(config.serviceRegistryConfig())
            .kafkaConfig(config.kafkaConfig())
            .postgresConfig(config.postgresConfig())
            .build();
    provider.init();
  }

  @AfterEach
  void after() {
    for (AutoCloseable closeable : provider.closeables()) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.warning("failed to close " + closeable);
      }
    }
  }

  protected <T> void persist(Function<Configuration, T> function) {
    DSLContext dslContext = provider.dslContext();

    new TransactionBoundary(dslContext) {
      {
        doInTransaction(function);
      }
    };
  }
}
