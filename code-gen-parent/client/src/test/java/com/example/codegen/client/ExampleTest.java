/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import com.example.codegen.advice.MeasureAdvice;
import org.junit.jupiter.api.Test;

class ExampleTest {

  @Test
  void test() {
    Example exampleAdvised =
        new Example_Advised(new DependencyA(), "", 1, new LogAdvice(), new MeasureAdvice());

    exampleAdvised.publicVoidMethod();
  }
}
