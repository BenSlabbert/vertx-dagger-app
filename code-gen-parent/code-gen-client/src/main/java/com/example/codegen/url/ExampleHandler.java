/* Licensed under Apache-2.0 2024. */
package com.example.codegen.url;

import com.example.codegen.annotation.url.RestHandler;
import io.vertx.ext.web.RoutingContext;
import java.time.Instant;

public class ExampleHandler {

  @RestHandler(path = "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}")
  public void pathHandler(RoutingContext ctx) {
    String path = ExampleHandler_PathHandler_ParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandler_PathHandler_ParamParser.ExampleHandler_PathHandler_Params params =
        ExampleHandler_PathHandler_ParamParser.parse(ctx);

    int param1 = params.param1();
    String param2 = params.param2();
    long param3 = params.param3();
  }

  @RestHandler(
      path =
          "/some/prefix?query1={int:query1=1}&query2={string:query2=abc}&query3={long:query3=4L}")
  public void queryHandler(RoutingContext ctx) {
    String path = ExampleHandler_QueryHandler_ParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandler_QueryHandler_ParamParser.ExampleHandler_QueryHandler_Params params =
        ExampleHandler_QueryHandler_ParamParser.parse(ctx);

    int query1 = params.query1();
    String query2 = params.query2();
    long query3 = params.query3();
  }

  @RestHandler(
      path =
          "/some/prefix/{int:param1=3}/path/{string:param2=cvb}/more-path/{long:param3}?query1={int:query1}&query2={string:query2}&query3={long:query3}")
  public void both(RoutingContext ctx) {
    String path = ExampleHandler_Both_ParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandler_Both_ParamParser.ExampleHandler_Both_Params params =
        ExampleHandler_Both_ParamParser.parse(ctx);

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
    String path = ExampleHandler_Types_ParamParser.PATH;
    System.err.println("path: " + path);
    ExampleHandler_Types_ParamParser.ExampleHandler_Types_Params params =
        ExampleHandler_Types_ParamParser.parse(ctx);

    float param1 = params.param1();
    double query1 = params.query1();
    boolean query2 = params.query2();
    Instant query3 = params.query3();
  }
}
