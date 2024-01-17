/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Custom {

  // must be string as we need a compile time constant
  // required by the code generator
  // <? extends Advice> this is the contract for the advisor
  String ADVISOR = "com.example.codegen.custom.CustomAdvice";

  // following params are optional,
  // but when present, they are used by the code generator
  // to customize the advisor
  String param1() default "";

  int foo() default 0;

  boolean bar() default false;
}
