/* Licensed under Apache-2.0 2023. */
package com.example.codegen.advice;

import dagger.Module;

@Module
public interface AdviceModule {

  DependencyB dependencyB();

  MeasureAdvice measureAdvice();
}
