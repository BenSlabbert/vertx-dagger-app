/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface RepositoryModule {

  @Binds
  SuggestionService providesItemRepository(RedisSuggestionService itemRepository);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableItemRepositoryImpl(RedisSuggestionService itemRepository);
}
