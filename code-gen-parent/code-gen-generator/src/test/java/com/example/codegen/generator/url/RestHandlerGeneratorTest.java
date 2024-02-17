/* Licensed under Apache-2.0 2024. */
package com.example.codegen.generator.url;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.example.codegen.generator.url.generator.RestHandlerGenerator;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import java.net.URL;
import org.junit.jupiter.api.Test;

class RestHandlerGeneratorTest {

  @Test
  void test() {
    URL resource = this.getClass().getClassLoader().getResource("RestHandlerTest.java");
    assertThat(resource).isNotNull();

    assertAbout(JavaSourceSubjectFactory.javaSource())
        .that(JavaFileObjects.forResource(resource))
        .processedWith(new RestHandlerGenerator())
        .compilesWithoutError();
  }
}
