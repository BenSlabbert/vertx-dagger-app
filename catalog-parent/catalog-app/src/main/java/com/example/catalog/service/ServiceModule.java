package com.example.catalog.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  @Binds
  ItemService createItemService(ItemServiceImpl itemService);

  @Binds
  Emitter createEmitter(RedisEmitter emitter);

  // https://stackoverflow.com/a/62025382
  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableRedisEmitter(RedisEmitter redisEmitter);
}
