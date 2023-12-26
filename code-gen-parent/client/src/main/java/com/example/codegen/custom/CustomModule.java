/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import dagger.Module;
import java.util.Map;

@Module(includes = {ModuleBindings.class})
public interface CustomModule {

  MyClass myClass();

  Map<Integer, MyClass> myClasses();
}
