/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.annotation.advice.Advice;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyAdvice implements Advice {

  @Inject
  MyAdvice() {}

  public void customize() {}

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {}

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {}
}
