/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface ModuleBindings {

  @Binds
  @IntoSet
  MyClass bindCustomAdvice(MyClass_CustomAdvice customAdvice);

  @Binds
  @IntoSet
  MyClass bindAdvised(MyClass_Advised advised);

  @Binds
  @IntoSet
  MyClass bind(MyClass myClass);
}
