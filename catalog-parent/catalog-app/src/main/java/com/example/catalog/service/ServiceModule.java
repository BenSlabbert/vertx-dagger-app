package com.example.catalog.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  @Binds
  ItemService createItemService(ItemServiceImpl itemService);

  @Binds
  Pool createPool(DBPool dbPool);

  @Binds
  Emitter createEmitter(RedisEmitter emitter);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableDBPool(DBPool dbPool);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableRedisEmitter(RedisEmitter redisEmitter);
}
