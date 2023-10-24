/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.config.ConfigModule;
import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.RepositoryModule;
import com.example.commons.kafka.KafkaModule;
import com.example.commons.saga.SagaModule;
import dagger.Component;
import io.vertx.pgclient.PgPool;
import io.vertx.redis.client.RedisAPI;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      RepositoryModule.class,
      ConfigModule.class,
      MapperModule.class,
      KafkaModule.class,
      SagaModule.class
    })
public interface TestPersistenceProvider extends Provider {

  ItemRepository itemRepository();

  PgPool pool();

  RedisAPI redisAPI();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestPersistenceProvider> {}
}
