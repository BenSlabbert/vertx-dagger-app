package com.example.catalog.service;

import dagger.Binds;
import dagger.Module;

@Module
public interface ServiceModule {

  @Binds
  ItemService createItemService(ItemServiceImpl itemService);

  @Binds
  Pool createPool(DBPool dbPool);
}
