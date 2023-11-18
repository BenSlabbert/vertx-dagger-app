/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import com.example.commons.ioc.DaggerProvider;
import com.example.commons.ioc.Provider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class TestBase {

  protected Provider provider;

  @BeforeEach
  void before(Vertx vertx) {
    provider = DaggerProvider.builder().vertx(vertx).build();
  }
}
