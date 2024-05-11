/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.web.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_ENCODING;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.codegen.annotation.url.RestHandler;
import com.example.commons.config.Config;
import com.example.jtehtmx.template.ExampleDto;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.output.Utf8ByteOutput;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.nio.charset.StandardCharsets;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ExampleHandler {

  private static final Logger log = LoggerFactory.getLogger(ExampleHandler.class);

  private final TemplateEngine templateEngine;
  private final Config config;

  @Inject
  ExampleHandler(Config config, TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
    this.config = config;
    test();
  }

  public void configureRoutes(Router router) {
    router.post(ExampleHandler_HandleClicked_ParamParser.PATH).handler(this::handleClicked);

    log.info("Configured routes for: " + this.getClass().getName());
    log.info("-------------------------");
    router
        .getRoutes()
        .forEach(
            route -> {
              log.info("Path: " + route.getPath());
              log.info("Methods: " + route.methods());
              log.info("-------------------------");
            });
  }

  @RestHandler(path = "/clicked")
  private void handleClicked(RoutingContext ctx) {
    TemplateOutput output =
        switch (config.profile()) {
          case DEV -> new StringOutput();
          case PROD -> new Utf8ByteOutput();
        };

    ExampleDto exampleDto = ExampleDto.builder().title("title").description("description").build();
    templateEngine.render("Example.jte", exampleDto, output);

    HttpServerResponse response = ctx.response();

    if (output instanceof Utf8ByteOutput byteOutput) {
      response.putHeader(CONTENT_LENGTH, Integer.toString(byteOutput.getContentLength()));
      response.putHeader(CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
      response.putHeader(CONTENT_ENCODING, "UTF-8");
      response.setStatusCode(OK.code());

      byte[] byteArray = byteOutput.toByteArray();
      Buffer buffer = Buffer.buffer(byteArray);
      response.end(buffer).onFailure(ctx::fail);
    } else if (output instanceof StringOutput stringOutput) {
      String string = stringOutput.toString();
      response.putHeader(CONTENT_LENGTH, Integer.toString(string.length()));
      response.putHeader(CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
      response.putHeader(CONTENT_ENCODING, "UTF-8");
      response.setStatusCode(OK.code());

      byte[] byteArray = string.getBytes(StandardCharsets.UTF_8);
      Buffer buffer = Buffer.buffer(byteArray);
      response.end(buffer).onFailure(ctx::fail);
    } else {
      response.setStatusCode(500);
      response.end("Internal Server Error");
    }
  }

  private void test() {
    switch (config.profile()) {
      case DEV -> {
        TemplateOutput strOutput = new StringOutput();
        ExampleDto exampleDto =
            ExampleDto.builder().title("title").description("description").build();
        templateEngine.render("Example.jte", exampleDto, strOutput);
        log.info("{}", strOutput);
      }
      case PROD -> {
        Utf8ByteOutput output = new Utf8ByteOutput();
        templateEngine.render("Example.jte", new ExampleDto("title", "description"), output);

        int contentLength = output.getContentLength();
        String string = new String(output.toByteArray(), StandardCharsets.UTF_8);
        log.info("contentLength: {}", contentLength);
        log.info(string);
      }
    }
  }
}
