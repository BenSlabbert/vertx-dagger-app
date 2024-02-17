/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.client.LogAdvice;
import com.example.codegen.generator.advice.annotation.Advised;
import javax.inject.Inject;

@Advised(advisors = {LogAdvice.class})
class OnlyAdvised {

  @Inject
  OnlyAdvised() {}

  public void method() {}
}
