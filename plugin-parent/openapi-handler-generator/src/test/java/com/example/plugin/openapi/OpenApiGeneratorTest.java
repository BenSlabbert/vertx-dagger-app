/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import com.example.plugin.openapi.type.In;
import com.example.plugin.openapi.type.Method;
import com.example.plugin.openapi.type.ParamType;
import com.example.plugin.openapi.type.SchemaType;
import java.io.File;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OpenApiGeneratorTest {

  @TempDir File temp;

  @Test
  void test() throws Exception {
    Generator generator = new OpenApiGenerator();
    generator.generate(
        List.of(
            new RequestResponseSchema(
                "/posts",
                Method.GET,
                List.of(
                    new ParameterSchema("path-int", ParamType.INTEGER, true, In.PATH),
                    new ParameterSchema("path-bool", ParamType.BOOLEAN, true, In.PATH),
                    new ParameterSchema("path-str", ParamType.STRING, true, In.PATH),
                    new ParameterSchema("query-int", ParamType.INTEGER, true, In.QUERY),
                    new ParameterSchema("query-bool", ParamType.BOOLEAN, true, In.QUERY),
                    new ParameterSchema("query-str", ParamType.STRING, true, In.QUERY)),
                Optional.empty(),
                new ResponseSchema(
                    200,
                    false,
                    SchemaType.ARRAY,
                    new ObjectSchema(
                        "post",
                        List.of(
                            new ObjectSchema.Property("id", ParamType.INTEGER, true),
                            new ObjectSchema.Property("name", ParamType.STRING, true),
                            new ObjectSchema.Property("registered", ParamType.BOOLEAN, true)))))),
        "com.example",
        temp);

    File[] files = temp.listFiles();

    //    assertThat(files).isNotNull().hasSize(1);
    //    File file = files[0];
    //    while (file.isDirectory()) {
    //      files = file.listFiles();
    //      assertThat(files).isNotNull();
    //      assertThat(files).hasSize(1);
    //      file = files[0];
    //    }
    //
    //    assertThat(file).satisfies(f -> assertThat(f).hasName("Post.java"));
  }
}
