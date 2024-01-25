/* Licensed under Apache-2.0 2024. */
package com.example.catalog.web.route.handler;

import dagger.Module;

@Module(includes = HandlerModuleBindings.class)
public interface HandlerModule {

  AuthHandler authHandler();

  ItemHandler itemHandler();
}
