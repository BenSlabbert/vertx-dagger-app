/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.tracing.opentelemetry.OpenTelemetryOptions;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PaymentAppLauncher extends Launcher {

  private static final Logger log = LoggerFactory.getLogger(PaymentAppLauncher.class);

  static {
    System.setProperty("org.jooq.no-tips", "true");
    System.setProperty("org.jooq.no-logo", "true");
  }

  public static void main(String[] args) {
    // breaks on native image
    // https://github.com/oracle/graal/issues/5510
    LocalDateTime parse =
        LocalDateTime.parse(
            "4714-11-24 00:00:00 BC",
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss G", Locale.ROOT));
    log.info("parse: " + parse);

    new PaymentAppLauncher().dispatch(args);
  }

  @Override
  public void afterStartingVertx(Vertx vertx) {
    log.info("afterStartingVertx");
    JsonObject config = vertx.getOrCreateContext().config();
    log.info("config: " + config);
  }

  @Override
  public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
    log.info("afterStartingVertx");
    if (deploymentOptions.getThreadingModel() != ThreadingModel.VIRTUAL_THREAD) {
      log.warn("deployment not configured as worker, overriding this setting");
      deploymentOptions.setThreadingModel(ThreadingModel.VIRTUAL_THREAD);
    }
  }

  @Override
  public void afterConfigParsed(JsonObject config) {
    log.info("afterConfigParsed");

    if (!config.isEmpty()) return;

    try (var input = getClass().getClassLoader().getResourceAsStream("application.json")) {
      if (null == input) {
        throw new NoStackTraceException("application.json not found");
      }

      log.info("loading default config");

      byte[] bytes = input.readAllBytes();
      JsonObject entries = new JsonObject(new String(bytes, StandardCharsets.UTF_8));
      config.mergeIn(entries, true);
    } catch (IOException e) {
      throw new NoStackTraceException(e);
    }
  }

  @Override
  public void beforeStartingVertx(VertxOptions options) {
    log.info("beforeStartingVertx");

    // setup tracing
    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder().build();
    OpenTelemetry openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
    options.setTracingOptions(new OpenTelemetryOptions(openTelemetry));

    // use native transport
    options.setPreferNativeTransport(true);
  }

  @Override
  public void beforeStoppingVertx(Vertx vertx) {
    log.info("beforeStoppingVertx");
  }

  @Override
  public void afterStoppingVertx() {
    log.info("afterStoppingVertx");
  }

  @Override
  public void handleDeployFailed(
      Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {

    log.error("handleDeployFailed", cause);

    super.handleDeployFailed(vertx, mainVerticle, deploymentOptions, cause);
  }
}
