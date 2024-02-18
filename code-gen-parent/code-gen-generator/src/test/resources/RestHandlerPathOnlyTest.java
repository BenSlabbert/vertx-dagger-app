/* Licensed under Apache-2.0 2024. */
package my.test;

import com.example.codegen.annotation.url.RestHandler;

public class RestHandlerPathOnlyTest {

  private static final String PATH = "/some/prefix/{int:param1}/path/{string:param2}/more-path";

  @RestHandler(path = PATH)
  public void handler() {}
}
