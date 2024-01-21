/* Licensed under Apache-2.0 2024. */
package com.example.reactivetest.web.handler;

import dagger.Binds;
import dagger.Module;

@Module
interface HandlerModuleBindings {

  @Binds
  PersonHandler personHandler(PersonHandlerImpl impl);
}
