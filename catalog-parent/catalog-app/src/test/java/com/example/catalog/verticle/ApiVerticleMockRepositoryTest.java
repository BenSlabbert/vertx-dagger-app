/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.catalog.MockRepositoryTest;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

class ApiVerticleMockRepositoryTest extends MockRepositoryTest {

  @Test
  void test1(Vertx vertx, VertxTestContext testContext) {
    assertThat("").isNotNull();
    testContext.completeNow();
  }

  @Test
  void test2(Vertx vertx, VertxTestContext testContext) {
    assertThat("").isNotNull();
    testContext.completeNow();
  }
}
