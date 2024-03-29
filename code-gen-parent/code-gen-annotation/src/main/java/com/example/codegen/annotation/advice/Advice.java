/* Licensed under Apache-2.0 2023. */
package com.example.codegen.annotation.advice;

public interface Advice {

  void before(Class<?> clazz, String methodName, Object... args);

  void after(Class<?> clazz, String methodName, Object result);
}
