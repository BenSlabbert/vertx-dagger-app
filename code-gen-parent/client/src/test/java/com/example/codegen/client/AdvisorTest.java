/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import java.util.Optional;
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

  @Test
  void inline() {
    AdvisorBeforeContract_Advised advisor =
        new AdvisorBeforeContract_Advised() {
          @Override
          public boolean publicVoidMethod(Advised advised) {
            return false;
          }

          @Override
          public Optional<String> publicStringMethod(Advised advised) {
            return Optional.empty();
          }

          @Override
          public boolean publicVoidStringMethod(String in, Advised advised) {
            return false;
          }

          @Override
          public boolean publicVoidStringMethod(String in1, String in2, Advised advised) {
            return false;
          }

          @Override
          public Optional<String> publicStringMethod(String in, Advised advised) {
            return Optional.empty();
          }

          @Override
          public Optional<String> publicStringMethod(String in1, String in2, Advised advised) {
            return Optional.empty();
          }
        };

    Advised advised = new BeforeAdvisor_Advised(advisor);
    advised.publicVoidMethod();
    advised.publicStringMethod();
    advised.publicVoidStringMethod("in");
    advised.publicVoidStringMethod("in1", "in2");
    advised.publicStringMethod("in");
    advised.publicStringMethod("in1", "in2");
  }
}
