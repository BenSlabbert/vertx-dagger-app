/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx;

import static github.benslabbert.vertxdaggercommons.FreePortUtility.getPort;

import com.example.jtehtmx.ioc.DaggerProvider;
import com.example.jtehtmx.ioc.Provider;
import github.benslabbert.vertxdaggercommons.ConfigEncoder;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class SimpleTest {

  protected static final int HTTP_PORT = getPort();

  @Test
  void deploy(Vertx vertx, VertxTestContext testContext) {
    Config config =
        Config.builder()
            .profile(Config.Profile.PROD)
            .httpConfig(Config.HttpConfig.builder().port(HTTP_PORT).build())
            .build();

    Provider provider =
        DaggerProvider.builder()
            .vertx(vertx)
            .httpConfig(config.httpConfig())
            .config(config)
            .build();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.jteHtmxVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
