/* Licensed under Apache-2.0 2024. */
package com.example.commons.closer;

import dagger.Module;

@Module
public interface CloserModule {

  ClosingService closingService();
}
