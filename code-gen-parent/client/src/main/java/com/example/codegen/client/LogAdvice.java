/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import com.example.codegen.generator.annotation.Advice;

class LogAdvice implements Advice {

  LogAdvice() {}

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {
    System.out.println("before " + clazz.getName() + "." + methodName);
  }

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {
    System.out.println("after " + clazz.getName() + "." + methodName + " result: " + result);
  }
}
