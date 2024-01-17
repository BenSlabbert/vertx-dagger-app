/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface MyAdvisor {

  String ADVISOR = "com.example.codegen.custom.MyAdvice";
}
