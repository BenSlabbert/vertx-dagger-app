/* Licensed under Apache-2.0 2024. */
package com.example.codegen.annotation.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface SecuredProxy {

  @interface SecuredAction {

    String group();

    String role();

    String[] permissions() default {};
  }
}
