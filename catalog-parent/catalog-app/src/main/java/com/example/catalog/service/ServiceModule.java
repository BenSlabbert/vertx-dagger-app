/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import dagger.Binds;
import dagger.Module;

@Module
public interface ServiceModule {

  @Binds
  ItemService createItemService(ItemService itemService);
}
