/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx.ioc;

import com.example.commons.config.Config;
import com.example.jtehtmx.config.JteConfig;
import com.example.jtehtmx.verticle.JteHtmxVerticle;
import com.example.jtehtmx.web.handler.ExampleHandler;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Component(modules = {JteConfig.class, Provider.EagerModule.class})
public interface Provider {

  @Nullable Void init();

  JteHtmxVerticle jteHtmxVerticle();

  ExampleHandler exampleHandler();

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

    private static final Logger log = LoggerFactory.getLogger(EagerModule.class);

    @Inject
    EagerModule() {}

    @Provides
    @Nullable static Void provideEager(Config config) {
      // this eagerly builds any parameters specified and returns nothing
      log.info("profile: " + config.profile());
      return null;
    }
  }
}
