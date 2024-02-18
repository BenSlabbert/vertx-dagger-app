/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.annotation.advice.Advised;
import com.example.codegen.client.LogAdvice;
import javax.inject.Inject;

@Advised(advisors = {LogAdvice.class})
class OnlyAdvised {

  @Inject
  OnlyAdvised() {}

  public void method() {}
}
