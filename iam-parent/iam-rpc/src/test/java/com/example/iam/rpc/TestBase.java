/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc;

import static com.example.commons.FreePortUtility.getPort;

import com.example.commons.ConfigEncoder;
import com.example.commons.config.Config;
import com.example.commons.config.Config.HttpConfig;
import com.example.iam.rpc.ioc.DaggerTestProvider;
import com.example.iam.rpc.ioc.TestProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public abstract class TestBase {

  protected static final int HTTP_PORT = getPort();

  protected TestProvider provider;

  @BeforeEach
  void prepare(Vertx vertx, VertxTestContext testContext) {
    Config config =
        Config.builder().httpConfig(HttpConfig.builder().port(HTTP_PORT).build()).build();

    provider = DaggerTestProvider.builder().vertx(vertx).config(config).build();

    JsonObject cfg = ConfigEncoder.encode(config);
    vertx.deployVerticle(
        provider.rpcVerticle(),
        new DeploymentOptions().setConfig(cfg),
        testContext.succeedingThenComplete());
  }
}
