/* Licensed under Apache-2.0 2024. */
package com.example.reactivetest.web.handler;

import dagger.Module;

@Module(includes = HandlerModuleBindings.class)
public interface HandlerModule {

  PersonHandler personHandler();
}
