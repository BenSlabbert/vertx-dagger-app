/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.commons.ConfigEncoder;
import com.example.payment.verticle.WorkerVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

public class VerticleTest extends PersistenceTest {

  @BeforeEach
  void before(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(
        WorkerVerticle::new,
        new DeploymentOptions()
            .setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
            .setConfig(ConfigEncoder.encode(config)),
        testContext.succeedingThenComplete());
  }

  @AfterEach
  @DisplayName("Check that the verticle is still there")
  void lastChecks(Vertx vertx) {
    assertThat(vertx.deploymentIDs()).isNotEmpty().hasSize(1);
  }
}
