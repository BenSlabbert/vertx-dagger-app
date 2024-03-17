/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.verticle;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.commons.auth.NoAuthRequiredAuthenticationProvider;
import com.example.commons.config.Config;
import com.example.commons.future.FutureUtil;
import com.example.jtehtmx.template.ExampleDto;
import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.output.Utf8ByteOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.inject.Inject;

public class JteHtmxVerticle extends AbstractVerticle {

  private static final Logger log = LoggerFactory.getLogger(JteHtmxVerticle.class);

  private final Config config;

  @Inject
  JteHtmxVerticle(Config config) {
    this.config = config;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    vertx.exceptionHandler(err -> log.error("unhandled exception", err));

    // used in dev mode
    // allows hot reloading of jte templates
    CodeResolver codeResolver =
        new DirectoryCodeResolver(
            Path.of(
                "/home/ben/IdeaProjects/vertx-dagger-app/jte-htmx-parent/jte-htmx/src/main/jte"));
    TemplateEngine te = TemplateEngine.create(codeResolver, ContentType.Html);
    TemplateOutput strOutput = new StringOutput();

    ExampleDto exampleDto = ExampleDto.builder().title("title").description("description").build();
    te.render("Example.jte", exampleDto, strOutput);
    System.out.println(strOutput);

    // used in prod mode
    // use precompiled jte templates
    if (false) {
      TemplateEngine templateEngine = TemplateEngine.createPrecompiled(ContentType.Html);
      templateEngine.setBinaryStaticContent(true);
      Utf8ByteOutput output = new Utf8ByteOutput();
      templateEngine.render("Example.jte", new ExampleDto("title", "description"), output);

      int contentLength = output.getContentLength();
      String string = new String(output.toByteArray(), StandardCharsets.UTF_8);
      System.out.println(contentLength);
      System.out.println(string);
    }

    Router mainRouter = Router.router(vertx);

    mainRouter
        .route()
        // CORS config
        .handler(CorsHandler.create())
        // 100kB max body size
        .handler(BodyHandler.create().setBodyLimit(1024L * 100L));

    mainRouter.get("/health*").handler(getHealthCheckHandler());
    mainRouter.get("/ping*").handler(getPingHandler());
    mainRouter
        .route()
        .handler(
            ctx -> {
              HttpMethod method = ctx.request().method();
              String path = ctx.request().path();
              String query = ctx.request().query();

              if (null == query) {
                log.info("[%s] %s".formatted(method, path));
              } else {
                log.info("[%s] %s?%s".formatted(method, path, query));
              }

              MultiMap parsedHeaderValues = ctx.request().headers();
              String htmxRequest = parsedHeaderValues.get("HX-Request");

              if (!Boolean.parseBoolean(htmxRequest)) {
                log.warn("request is not an htmx request");
                ctx.fail(BAD_REQUEST.code());
                return;
              }

              ctx.next();
            });

    mainRouter
        .post("/clicked")
        .handler(
            ctx -> {
              HttpServerResponse response = ctx.response();
              String output = strOutput.toString();
              response.putHeader(CONTENT_LENGTH, Integer.toString(output.length()));
              response.putHeader(CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
              response.putHeader(CONTENT_ENCODING, "UTF-8");
              response.setStatusCode(OK.code());

              Buffer buffer = Buffer.buffer(output);
              response.end(buffer).onFailure(ctx::fail);
            });

    Config.HttpConfig httpConfig = config.httpConfig();
    log.info("starting api verticle on port: " + httpConfig.port());
    vertx
        .createHttpServer(new HttpServerOptions().setPort(httpConfig.port()).setHost("0.0.0.0"))
        .requestHandler(mainRouter)
        .listen(
            res -> {
              if (res.succeeded()) {
                log.info("started http server");
                startPromise.complete();
              } else {
                log.error("failed to start verticle", res.cause());
                startPromise.fail(res.cause());
              }
            });
  }

  private HealthCheckHandler getPingHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("ping", promise -> promise.complete(Status.OK()));
  }

  private HealthCheckHandler getHealthCheckHandler() {
    return HealthCheckHandler.create(vertx, NoAuthRequiredAuthenticationProvider.create())
        .register("available", promise -> promise.complete(Status.OK()));
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void stop(Promise<Void> stopPromise) {
    System.err.println("stopping");
    System.err.println("awaitTermination...start");
    FutureUtil.awaitTermination()
        .onComplete(
            ar -> {
              System.err.printf("awaitTermination...end: %b%n", ar.result());
              stopPromise.complete();
            });
  }
}
