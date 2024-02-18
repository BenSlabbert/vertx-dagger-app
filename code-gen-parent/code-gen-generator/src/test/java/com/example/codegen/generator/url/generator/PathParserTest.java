/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url.generator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PathParserTest {

  @Test
  void pathTest() {
    String path = "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}";
    PathParser.ParseResult parseResult = PathParser.parse(path);

    assertThat(parseResult).isNotNull();
    assertThat(parseResult.pathParams()).hasSize(3);
    assertThat(parseResult.pathParams().get(0).name()).isEqualTo("param1");
    assertThat(parseResult.pathParams().get(0).type()).isEqualTo(PathParser.Type.INT);
    assertThat(parseResult.pathParams().get(1).name()).isEqualTo("param2");
    assertThat(parseResult.pathParams().get(1).type()).isEqualTo(PathParser.Type.STRING);
    assertThat(parseResult.pathParams().get(2).name()).isEqualTo("param3");
    assertThat(parseResult.pathParams().get(2).type()).isEqualTo(PathParser.Type.LONG);
  }

  @Test
  void queryTest() {
    String path = "/some/path?query1={int:query1}&query2={string:query2}&query3={long:query3}";
    PathParser.ParseResult parseResult = PathParser.parse(path);

    assertThat(parseResult).isNotNull();
    assertThat(parseResult.queryParams()).hasSize(3);
    assertThat(parseResult.queryParams().get(0).name()).isEqualTo("query1");
    assertThat(parseResult.queryParams().get(0).type()).isEqualTo(PathParser.Type.INT);
    assertThat(parseResult.queryParams().get(1).name()).isEqualTo("query2");
    assertThat(parseResult.queryParams().get(1).type()).isEqualTo(PathParser.Type.STRING);
    assertThat(parseResult.queryParams().get(2).name()).isEqualTo("query3");
    assertThat(parseResult.queryParams().get(2).type()).isEqualTo(PathParser.Type.LONG);
  }

  @Test
  void bothTest() {
    String path =
        "/some/prefix/{int:param1}/path/{string:param2}/more-path/{long:param3}?query1={int:query1}&query2={string:query2}&query3={long:query3}";
    PathParser.ParseResult parseResult = PathParser.parse(path);

    assertThat(parseResult).isNotNull();
    assertThat(parseResult.pathParams()).hasSize(3);
    assertThat(parseResult.pathParams().get(0).name()).isEqualTo("param1");
    assertThat(parseResult.pathParams().get(0).type()).isEqualTo(PathParser.Type.INT);
    assertThat(parseResult.pathParams().get(1).name()).isEqualTo("param2");
    assertThat(parseResult.pathParams().get(1).type()).isEqualTo(PathParser.Type.STRING);
    assertThat(parseResult.pathParams().get(2).name()).isEqualTo("param3");
    assertThat(parseResult.pathParams().get(2).type()).isEqualTo(PathParser.Type.LONG);

    assertThat(parseResult.queryParams()).hasSize(3);
    assertThat(parseResult.queryParams().get(0).name()).isEqualTo("query1");
    assertThat(parseResult.queryParams().get(0).type()).isEqualTo(PathParser.Type.INT);
    assertThat(parseResult.queryParams().get(1).name()).isEqualTo("query2");
    assertThat(parseResult.queryParams().get(1).type()).isEqualTo(PathParser.Type.STRING);
    assertThat(parseResult.queryParams().get(2).name()).isEqualTo("query3");
    assertThat(parseResult.queryParams().get(2).type()).isEqualTo(PathParser.Type.LONG);
  }
}
