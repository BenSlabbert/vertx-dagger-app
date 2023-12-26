/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.generator;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;

import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourceSubjectFactory;
import java.net.URL;
import org.junit.jupiter.api.Test;

class AdviceGeneratorTest {

  @Test
  void test() {
    URL resource = this.getClass().getClassLoader().getResource("AdviceTest.java");
    assertThat(resource).isNotNull();

    assertAbout(JavaSourceSubjectFactory.javaSource())
        .that(JavaFileObjects.forResource(resource))
        .withCompilerOptions("-AprocessCustom=com.example.codegen.custom.Custom")
        .processedWith(new AdviceGenerator())
        .compilesWithoutError();
  }
}
