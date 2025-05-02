/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx;

import com.example.jtehtmx.ioc.DaggerProvider;
import com.example.jtehtmx.ioc.Provider;
import com.example.jtehtmx.verticle.JteHtmxVerticle;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import github.benslabbert.vertxdaggercommons.config.Config;
import github.benslabbert.vertxdaggercommons.test.ConfigEncoder;
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

  private JteHtmxVerticle verticle;

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
    Config config =
        Config.builder()
            .profile(Config.Profile.PROD)
            .httpConfig(Config.HttpConfig.builder().port(0).build())
            .build();

    Provider provider =
        DaggerProvider.builder()
            .vertx(vertx)
            .httpConfig(config.httpConfig())
            .config(config)
            .build();

    JsonObject cfg = ConfigEncoder.encode(config);
    verticle = provider.jteHtmxVerticle();
    vertx
        .deployVerticle(verticle, new DeploymentOptions().setConfig(cfg))
        .onComplete(testContext.succeedingThenComplete());
  }

  @AfterEach
  void closeContext() {
    context.close();
  }

  @Test
  void test() {
    page.navigate("http://127.0.0.1:%d/".formatted(verticle.getPort()));
    page.locator("h1").getByText("Welcome to SvelteKit").waitFor();
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("about")).click();
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("home")).click();
    page.locator("h1").getByText("Welcome to SvelteKit").waitFor();
  }
}
