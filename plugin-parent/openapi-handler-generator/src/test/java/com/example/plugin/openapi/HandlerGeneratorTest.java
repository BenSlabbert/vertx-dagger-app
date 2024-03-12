/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Test;

class HandlerGeneratorTest {

  @Test
  void test() throws MojoExecutionException {
    URL resource = HandlerGeneratorTest.class.getClassLoader().getResource("api.yaml");
    assertThat(resource).isNotNull();

    Path path = Paths.get(resource.getPath());
    File file = path.toFile();
    assertThat(file).isNotNull();

    OpenApiSchemaParser handlerGenerator = new OpenApiSchemaParser();
    List<RequestResponseSchema> requestResponseSchemas = handlerGenerator.parseFile(file);

    assertThat(requestResponseSchemas).hasSize(5);
    assertPostsGetSchema(requestResponseSchemas.get(0));
    assertPostsPostSchema(requestResponseSchemas.get(1));
    assertPostsIdGetSchema(requestResponseSchemas.get(2));
    assertPostsIdPatchSchema(requestResponseSchemas.get(3));
    assertPostsIdDeleteSchema(requestResponseSchemas.get(4));
  }

  private void assertPostsIdDeleteSchema(RequestResponseSchema requestResponseSchema) {
    RequestResponseSchema expected =
        new RequestResponseSchema(
            "/posts/{post-id}",
            Method.DELETE,
            List.of(new ParameterSchema("post-id", ParamType.INTEGER, true, In.PATH)),
            Optional.empty(),
            new ResponseSchema(204, true, null, null));

    assertThat(requestResponseSchema).usingRecursiveComparison().isEqualTo(expected);
  }

  private void assertPostsIdPatchSchema(RequestResponseSchema requestResponseSchema) {
    RequestResponseSchema expected =
        new RequestResponseSchema(
            "/posts/{post-id}",
            Method.PATCH,
            List.of(new ParameterSchema("post-id", ParamType.INTEGER, true, In.PATH)),
            Optional.of(
                new RequestBodySchema(
                    true,
                    new ObjectSchema(
                        "post",
                        List.of(
                            new ObjectSchema.Property("userId", ParamType.INTEGER, true),
                            new ObjectSchema.Property("id", ParamType.INTEGER, true),
                            new ObjectSchema.Property("title", ParamType.STRING, true),
                            new ObjectSchema.Property("body", ParamType.STRING, true))))),
            new ResponseSchema(
                200,
                false,
                SchemaType.OBJECT,
                new ObjectSchema(
                    "post",
                    List.of(
                        new ObjectSchema.Property("userId", ParamType.INTEGER, true),
                        new ObjectSchema.Property("id", ParamType.INTEGER, true),
                        new ObjectSchema.Property("title", ParamType.STRING, true),
                        new ObjectSchema.Property("body", ParamType.STRING, true)))));

    assertThat(requestResponseSchema).usingRecursiveComparison().isEqualTo(expected);
  }

  private void assertPostsIdGetSchema(RequestResponseSchema requestResponseSchema) {
    RequestResponseSchema expected =
        new RequestResponseSchema(
            "/posts/{post-id}",
            Method.GET,
            List.of(new ParameterSchema("post-id", ParamType.INTEGER, true, In.PATH)),
            Optional.empty(),
            new ResponseSchema(
                200,
                false,
                SchemaType.OBJECT,
                new ObjectSchema(
                    "post",
                    List.of(
                        new ObjectSchema.Property("userId", ParamType.INTEGER, true),
                        new ObjectSchema.Property("id", ParamType.INTEGER, true),
                        new ObjectSchema.Property("title", ParamType.STRING, true),
                        new ObjectSchema.Property("body", ParamType.STRING, true)))));

    assertThat(requestResponseSchema).usingRecursiveComparison().isEqualTo(expected);
  }

  private void assertPostsPostSchema(RequestResponseSchema requestResponseSchema) {
    RequestResponseSchema expected =
        new RequestResponseSchema(
            "/posts",
            Method.POST,
            List.of(),
            Optional.of(
                new RequestBodySchema(
                    true,
                    new ObjectSchema(
                        "post",
                        List.of(
                            new ObjectSchema.Property("userId", ParamType.INTEGER, true),
                            new ObjectSchema.Property("id", ParamType.INTEGER, true),
                            new ObjectSchema.Property("title", ParamType.STRING, true),
                            new ObjectSchema.Property("body", ParamType.STRING, true))))),
            new ResponseSchema(
                201,
                false,
                SchemaType.OBJECT,
                new ObjectSchema(
                    "post",
                    List.of(
                        new ObjectSchema.Property("userId", ParamType.INTEGER, true),
                        new ObjectSchema.Property("id", ParamType.INTEGER, true),
                        new ObjectSchema.Property("title", ParamType.STRING, true),
                        new ObjectSchema.Property("body", ParamType.STRING, true)))));

    assertThat(requestResponseSchema).usingRecursiveComparison().isEqualTo(expected);
  }

  private void assertPostsGetSchema(RequestResponseSchema actual) {
    RequestResponseSchema expected =
        new RequestResponseSchema(
            "/posts",
            Method.GET,
            List.of(
                new ParameterSchema("userId", ParamType.INTEGER, false, In.QUERY),
                new ParameterSchema("title", ParamType.STRING, false, In.QUERY)),
            Optional.empty(),
            new ResponseSchema(
                200,
                false,
                SchemaType.ARRAY,
                new ObjectSchema(
                    "post",
                    List.of(
                        new ObjectSchema.Property("userId", ParamType.INTEGER, true),
                        new ObjectSchema.Property("id", ParamType.INTEGER, true),
                        new ObjectSchema.Property("title", ParamType.STRING, true),
                        new ObjectSchema.Property("body", ParamType.STRING, true)))));

    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
