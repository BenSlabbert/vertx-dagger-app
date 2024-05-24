/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.SuggestionService;
import com.example.catalog.service.ItemService;
import com.example.catalog.service.ServiceModule;
import com.example.catalog.web.WebModule;
import com.example.catalog.web.route.handler.HandlerModule;
import dagger.BindsInstance;
import dagger.Component;
import github.benslabbert.vertxdaggercommons.saga.SagaModule;
import io.vertx.redis.client.RedisAPI;
import io.vertx.sqlclient.Pool;
import java.util.Set;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(
    modules = {
      MapperModule.class,
      SagaModule.class,
      ServiceModule.class,
      HandlerModule.class,
      WebModule.class,
      Provider.EagerModule.class
    })
public interface TestMockRepositoryProvider extends Provider {

  ItemService itemService();

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestMockRepositoryProvider> {

    @BindsInstance
    Builder suggestionService(SuggestionService suggestionService);

    @BindsInstance
    Builder itemRepository(ItemRepository itemRepository);

    @BindsInstance
    Builder pool(Pool pool);

    @BindsInstance
    Builder redisAPI(RedisAPI redisAPI);

    @BindsInstance
    Builder closeables(Set<AutoCloseable> closeables);

    @BindsInstance
    Builder dslContext(@Named("static") DSLContext dslContext);

    TestMockRepositoryProvider build();
  }
}
