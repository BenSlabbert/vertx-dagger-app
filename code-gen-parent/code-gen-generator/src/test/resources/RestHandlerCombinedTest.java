/* Licensed under Apache-2.0 2024. */
package my.test;

import com.example.codegen.generator.url.annotation.RestHandler;

public class RestHandlerCombinedTest {

  private static final String PATH =
      "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}?query1={int:query1}&query2={string:query2}&query3={long:query3}";

  @RestHandler(path = PATH)
  public void handler() {}
}
