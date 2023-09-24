/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.integration.AuthenticationIntegration;
import com.example.catalog.repository.SuggestionService;
import com.example.catalog.service.ServiceModule;
import com.example.commons.config.Config;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgPool;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      ServiceModule.class,
    })
public interface TestMockRepositoryProvider extends Provider {

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder httpConfig(Config.HttpConfig httpConfig);

    @BindsInstance
    Builder redisConfig(Config.RedisConfig redisConfig);

    @BindsInstance
    Builder verticleConfig(Config.VerticleConfig verticleConfig);

    @BindsInstance
    Builder serviceRegistryConfig(Map<Config.ServiceIdentifier, Config.ServiceRegistryConfig> map);

    @BindsInstance
    Builder authenticationIntegration(AuthenticationIntegration authenticationIntegration);

    @BindsInstance
    Builder suggestionService(SuggestionService suggestionService);

    @BindsInstance
    Builder pgPool(PgPool pgPool);

    @BindsInstance
    Builder closeables(Set<AutoCloseable> closeables);

    @BindsInstance
    Builder dslContext(DSLContext dslContext);

    TestMockRepositoryProvider build();
  }
}
