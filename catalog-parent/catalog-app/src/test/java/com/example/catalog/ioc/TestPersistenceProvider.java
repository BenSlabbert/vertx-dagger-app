/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.config.ConfigModule;
import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceModule;
import com.example.commons.saga.SagaModule;
import dagger.Component;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.Set;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      RepositoryModule.class,
      ConfigModule.class,
      MapperModule.class,
      ServiceModule.class,
      SagaModule.class,
      Provider.EagerModule.class
    })
public interface TestPersistenceProvider extends Provider {

  ItemRepository itemRepository();

  Pool pool();

  RedisAPI redisAPI();

  Set<AutoCloseable> closeables();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestPersistenceProvider> {}
}
