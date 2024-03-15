/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.ioc;

import com.example.commons.config.Config;
import com.example.jtehtmx.verticle.JteHtmxVerticle;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(modules = {Provider.EagerModule.class})
public interface Provider {

  @Nullable Void init();

  JteHtmxVerticle jteHtmxVerticle();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder config(Config config);

    @BindsInstance
    Builder httpConfig(Config.HttpConfig httpConfig);

    Provider build();
  }

  @Module
  final class EagerModule {

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager() {
      // this eagerly builds any parameters specified and returns nothing
      return null;
    }
  }
}
