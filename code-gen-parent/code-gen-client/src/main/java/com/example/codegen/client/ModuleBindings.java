/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import dagger.Binds;
import dagger.Module;

@Module
interface ModuleBindings {

  @Binds
  Example example(Example_Advised advised);
}
