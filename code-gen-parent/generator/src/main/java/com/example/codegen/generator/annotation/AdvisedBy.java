/* Licensed under Apache-2.0 2023. */
package com.example.codegen.generator.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface AdvisedBy {

  boolean before() default false;

  boolean after() default false;

  boolean around() default false;
}
