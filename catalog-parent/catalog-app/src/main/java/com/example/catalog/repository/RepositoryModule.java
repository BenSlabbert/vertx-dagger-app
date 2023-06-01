package com.example.catalog.repository;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface RepositoryModule {

  @Binds
  ItemRepository providesItemRepository(ItemRepositoryImpl itemRepository);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableItemRepositoryImpl(ItemRepositoryImpl itemRepository);
}
