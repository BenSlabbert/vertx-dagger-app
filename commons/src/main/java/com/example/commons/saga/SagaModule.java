/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import dagger.Module;

@Module
public interface SagaModule {

  SagaBuilder sagaBuilder();
}
