/* Licensed under Apache-2.0 2024. */
package com.example.codegen.url;

import com.example.codegen.generator.url.annotation.RestHandler;
import io.vertx.ext.web.RoutingContext;

public class ExampleHandler {

  @RestHandler(path = "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}")
  public void handler(RoutingContext ctx) {
    String path = ExampleHandlerHandlerParamParser.PATH;
    ExampleHandlerHandlerParamParser.ExampleHandlerHandlerParams params =
        ExampleHandlerHandlerParamParser.parse(ctx);

    int param1 = params.param1();
    String param2 = params.param2();
    long param3 = params.param3();
  }
}
