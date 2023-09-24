/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.config.ConfigModule;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceModule;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
      RepositoryModule.class,
      ConfigModule.class,
    })
public interface TestPersistenceProvider extends Provider {

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestPersistenceProvider> {}
}
