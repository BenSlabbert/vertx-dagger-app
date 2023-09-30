/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.mapper.MapperModule;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.SuggestionService;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.pgclient.PgPool;
import io.vertx.redis.client.RedisAPI;
import java.util.Set;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
@Component(modules = {MapperModule.class})
public interface TestMockRepositoryProvider extends Provider {

  @Component.Builder
  interface Builder extends BaseBuilder<Builder, TestMockRepositoryProvider> {

    @BindsInstance
    Builder suggestionService(SuggestionService suggestionService);

    @BindsInstance
    Builder itemRepository(ItemRepository itemRepository);

    @BindsInstance
    Builder pgPool(PgPool pgPool);

    @BindsInstance
    Builder redisAPI(RedisAPI redisAPI);

    @BindsInstance
    Builder closeables(Set<AutoCloseable> closeables);

    @BindsInstance
    Builder dslContext(DSLContext dslContext);

    TestMockRepositoryProvider build();
  }
}
