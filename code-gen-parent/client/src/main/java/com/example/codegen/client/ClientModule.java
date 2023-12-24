/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import com.example.codegen.advice.AdviceModule;
import dagger.Module;

@Module(includes = {AdviceModule.class, ModuleBindings.class})
public interface ClientModule {

  Example example();
}
