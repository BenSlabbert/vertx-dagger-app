/* Licensed under Apache-2.0 2023. */
package com.example.commons.ioc;

import com.example.commons.saga.SagaBuilder;
import com.example.commons.saga.SagaModule;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import javax.inject.Singleton;

@Singleton
@Component(modules = {SagaModule.class})
public interface Provider {

  SagaBuilder sagaBuilder();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    Provider build();
  }
}
