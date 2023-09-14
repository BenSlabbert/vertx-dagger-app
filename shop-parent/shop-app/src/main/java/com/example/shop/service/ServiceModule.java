/* Licensed under Apache-2.0 2023. */
package com.example.shop.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableItemEventStreamConsumer(ItemEventStreamConsumer itemRepository);
}
