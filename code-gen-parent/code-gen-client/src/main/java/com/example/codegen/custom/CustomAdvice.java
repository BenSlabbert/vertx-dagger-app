/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.generator.advice.annotation.Advice;
import java.util.Arrays;
import javax.inject.Inject;

// do not make singleton
// should be instantiated for every invocation of the advisor
public class CustomAdvice implements Advice {

  private String param1;
  private int foo;
  private boolean bar;

  @Inject
  CustomAdvice() {}

  // these are the additional params that can be used to customize the advisor
  // there is no interface for this, but since we use compile time generation
  // we will get fast feedback if the advisor is not implemented correctly
  public void customize(String param1, int foo, boolean bar) {
    // called by the code generator
    // treat this as a PostInitialize method
    // will be called for every invocation of the advisor on an advised method
    this.param1 = param1;
    this.foo = foo;
    this.bar = bar;
  }

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {
    System.err.println("before: " + clazz + " " + methodName + " args: " + Arrays.toString(args));
    System.err.println("param1: " + param1);
    System.err.println("foo: " + foo);
    System.err.println("bar: " + bar);
  }

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {
    System.err.println("after: " + clazz + " " + methodName + " result: " + result);
    System.err.println("param1: " + param1);
    System.err.println("foo: " + foo);
    System.err.println("bar: " + bar);
  }
}
