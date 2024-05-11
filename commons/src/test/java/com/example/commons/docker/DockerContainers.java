/* Licensed under Apache-2.0 2024. */
package com.example.commons.docker;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public final class DockerContainers {

  private DockerContainers() {}

  public static final Network network = Network.newNetwork();

  public static final GenericContainer<?> POSTGRES =
      new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
          .withExposedPorts(5432)
          .withNetwork(network)
          .withNetworkAliases("postgres")
          .withEnv("POSTGRES_USER", "postgres")
          .withEnv("POSTGRES_PASSWORD", "postgres")
          .withEnv("POSTGRES_DB", "postgres")
          // must wait twice as the init process also prints this message
          .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2));

  public static final GenericContainer<?> REDIS =
      new GenericContainer<>(DockerImageName.parse("redis/redis-stack-server:latest"))
          .withExposedPorts(6379)
          .withNetwork(network)
          .withNetworkAliases("redis")
          .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1));
}
