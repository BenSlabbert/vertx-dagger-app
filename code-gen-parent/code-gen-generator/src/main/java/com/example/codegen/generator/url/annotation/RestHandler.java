/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface RestHandler {

  /**
   * path must be in the form of /some/prefix/{int:param1}/path/{string:param2}/more-path <br>
   * in this example, the path has two parameters, int and one String <br>
   * the code generator will generate a record type as follows: <br>
   * record MethodNameParams(int param1, String param2) {} <br>
   * as well as a class to parse and create this record type with the name <br>
   * if the class name is UserHandler and the method name is login, the generated class <br>
   * will be: UserHandler_Login_ParamParser <br>
   * with a single static method parse(io.vertx.ext.web.RoutingContext) that returns the record type
   * <br>
   * generated class will create a sanitized path that can be used by the io.vertx.ext.web.Router
   */
  // todo: add query parameters
  String path();
}
