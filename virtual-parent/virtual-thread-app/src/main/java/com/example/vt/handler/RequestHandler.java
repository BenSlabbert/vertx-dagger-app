/* Licensed under Apache-2.0 2024. */
package com.example.vt.handler;

import github.benslabbert.vertxdaggercodegen.annotation.url.RestHandler;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

  private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

  public void configure(Router router) {
    // todo maybe have a code generator to do this configuration
    router.post(RequestHandler_HandlePing_ParamParser.PATH).blockingHandler(this::handlePing);
  }

  // something like this maybe
  //  static class Configurer {
  //
  //    static void configure(Router router, RequestHandler requestHandler) {
  //      router
  //          .post(RequestHandler_HandlePing_ParamParser.PATH)
  //          .blockingHandler(requestHandler::handlePing);
  //    }
  //  }

  @RestHandler(path = "/ping")
  private void handlePing(RoutingContext ctx) {
    log.info("ping handler");
    var string = ctx.body().asJsonObject();
    log.info("body: " + string);
    ctx.response().end("pong");
  }
}
