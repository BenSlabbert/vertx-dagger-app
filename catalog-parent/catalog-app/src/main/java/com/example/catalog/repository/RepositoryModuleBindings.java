/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository;

import com.example.catalog.repository.sql.ItemRepositoryImpl;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface RepositoryModuleBindings {

  @Binds
  SuggestionService providesSuggestionService(RedisSuggestionService itemRepository);

  @Binds
  ItemRepository providesItemRepository(ItemRepositoryImpl itemRepository);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableItemRepositoryImpl(RedisSuggestionService itemRepository);
}
