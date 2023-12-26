/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.codegen.advice.DependencyB;
import com.example.codegen.advice.MeasureAdvice;
import com.example.codegen.ioc.DaggerTestProvider;
import com.example.codegen.ioc.TestProvider;
import org.junit.jupiter.api.Test;

class ExampleTest {

  @Test
  void test() {
    TestProvider provider = DaggerTestProvider.builder().string("string").integer(1).build();

    Example example = provider.example();
    DependencyB dependencyB1 = provider.dependencyB();
    DependencyB dependencyB2 = provider.dependencyB();
    MeasureAdvice measureAdvice1 = provider.measureAdvice();
    MeasureAdvice measureAdvice2 = provider.measureAdvice();

    assertThat(example).isInstanceOf(Example_Advised.class);

    example.publicVoidMethod();
    example.publicStringMethod(new DependencyA(), 1, "string");
    example.returnList("in");
    example.returnMap();
  }
}
