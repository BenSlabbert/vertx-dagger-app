/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.config.ConfigModule;
import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceModule;
import com.example.catalog.web.WebModule;
import com.example.catalog.web.route.handler.HandlerModule;
import com.example.commons.saga.SagaModule;
import dagger.Component;
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
      HandlerModule.class,
      WebModule.class,
      Provider.EagerModule.class
    })
public interface TestPersistenceProvider extends Provider {

  ItemRepository itemRepository();

  Set<AutoCloseable> closeables();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestPersistenceProvider> {}
}
