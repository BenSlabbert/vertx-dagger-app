/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.annotation.advice.Advised;
import com.example.codegen.client.LogAdvice;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Advised(advisors = {LogAdvice.class})
class MyClass {

  @Inject
  MyClass() {}

  @Custom(param1 = "foo", bar = true)
  public void method() {}
}
