package com.example.catalog.repository;

import dagger.Binds;
import dagger.Module;

@Module
public interface RepositoryModule {

  @Binds
  ItemRepository providesItemRepository(ItemRepositoryImpl itemRepository);
}
