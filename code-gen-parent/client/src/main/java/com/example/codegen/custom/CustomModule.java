/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import dagger.Module;

@Module(includes = ModuleBindings.class)
public interface CustomModule {

  Client client();
}
