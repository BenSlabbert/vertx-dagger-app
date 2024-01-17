/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import dagger.Binds;
import dagger.Module;

@Module
interface ModuleBindings {

  @Binds
  MyClass myClass(MyClass_Advised myClass);

  @Binds
  OnlyAdvised onlyAdvised(OnlyAdvised_Advised myClass);

  @Binds
  OnlyCustom onlyCustom(OnlyCustom_Advised myClass);
}
