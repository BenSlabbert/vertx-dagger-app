/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx;

import static github.benslabbert.vertxdaggercommons.FreePortUtility.getPort;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.jtehtmx.ioc.DaggerProvider;
import com.example.jtehtmx.ioc.Provider;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import github.benslabbert.vertxdaggercommons.ConfigEncoder;
import github.benslabbert.vertxdaggercommons.config.Config;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class FullStackTest {

  private static Playwright playwright;
  private static Browser browser;
  private static final int HTTP_PORT = getPort();

  private Config config;

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    browser = playwright.chromium().launch();
  }

  @AfterAll
  static void closeBrowser() {
    playwright.close();
  }

  // New instance for each test method.
  BrowserContext context;
  Page page;

  @BeforeEach
  void createContextAndPage(Vertx vertx, VertxTestContext testContext) {
    context = browser.newContext();
    page = context.newPage();
    config =
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

  @AfterEach
  void closeContext() {
    context.close();
  }

  @Test
  void test() {
    page.navigate("http://127.0.0.1:" + config.httpConfig().port());
    page.waitForCondition(page.querySelector("h1")::isVisible);

    String h1 = page.querySelector("h1").innerText();
    assertThat(h1).isEqualTo("Welcome to SvelteKit");

    page.click("text=about");
    page.click("text=home");
    page.click("text=about");
  }
}
