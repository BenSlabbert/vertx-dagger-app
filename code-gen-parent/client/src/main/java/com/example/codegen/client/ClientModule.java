/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import dagger.Module;

@Module(includes = ModuleBindings.class)
public interface ClientModule {

  Example example();
}
