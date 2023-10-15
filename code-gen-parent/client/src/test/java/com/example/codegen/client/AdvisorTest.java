/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import org.junit.jupiter.api.Test;

class AdvisorTest {

  @Test
  void test() {
    Advisor advisor = new Advisor();
    Advised advised = new BeforeAdvisor_Advised(advisor);
    advised.publicVoidMethod();
    advised.publicStringMethod();
    advised.publicVoidStringMethod("in");
    advised.publicVoidStringMethod("in1", "in2");
    advised.publicStringMethod("in");
    advised.publicStringMethod("in1", "in2");
  }
}
