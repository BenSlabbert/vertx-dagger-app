/* Licensed under Apache-2.0 2024. */
package com.example.catalog.web.route.handler;

import dagger.Binds;
import dagger.Module;

@Module
interface HandlerModuleBindings {

  @Binds
  ItemHandler itemHandler(ItemHandlerImpl impl);
}
