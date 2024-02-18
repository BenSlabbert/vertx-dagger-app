/* Licensed under Apache-2.0 2024. */
package com.example.codegen.url;

import com.example.codegen.generator.url.annotation.RestHandler;
import io.vertx.ext.web.RoutingContext;
import java.time.Instant;

public class ExampleHandler {

  @RestHandler(path = "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}")
  public void pathHandler(RoutingContext ctx) {
    String path = ExampleHandlerPathHandlerParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandlerPathHandlerParamParser.ExampleHandlerPathHandlerParams params =
        ExampleHandlerPathHandlerParamParser.parse(ctx);

    int param1 = params.param1();
    String param2 = params.param2();
    long param3 = params.param3();
  }

  @RestHandler(
      path = "/some/prefix?query1={int:query1}&query2={string:query2}&query3={long:query3}")
  public void queryHandler(RoutingContext ctx) {
    String path = ExampleHandlerQueryHandlerParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandlerQueryHandlerParamParser.ExampleHandlerQueryHandlerParams params =
        ExampleHandlerQueryHandlerParamParser.parse(ctx);

    int query1 = params.query1();
    String query2 = params.query2();
    long query3 = params.query3();
  }

  @RestHandler(
      path =
          "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}?query1={int:query1}&query2={string:query2}&query3={long:query3}")
  public void both(RoutingContext ctx) {
    String path = ExampleHandlerBothParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandlerBothParamParser.ExampleHandlerBothParams params =
        ExampleHandlerBothParamParser.parse(ctx);

    int param1 = params.query1();
    String param2 = params.query2();
    long param3 = params.query3();
    int query1 = params.query1();
    String query2 = params.query2();
    long query3 = params.query3();
  }

  @RestHandler(
      path =
          "/some/prefix/{float:param1}?query1={double:query1}&query2={boolean:query2}&query3={ts:query3}")
  public void types(RoutingContext ctx) {
    String path = ExampleHandlerTypesParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandlerTypesParamParser.ExampleHandlerTypesParams params =
        ExampleHandlerTypesParamParser.parse(ctx);

    float param1 = params.param1();
    double query1 = params.query1();
    boolean query2 = params.query2();
    Instant query3 = params.query3();
  }
}
