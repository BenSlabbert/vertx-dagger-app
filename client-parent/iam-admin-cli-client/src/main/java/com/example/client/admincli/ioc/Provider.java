/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.ioc;

import com.example.client.admincli.auth.AuthModule;
import com.example.client.admincli.command.CommandModule;
import com.example.client.admincli.config.ConfigModule;
import com.example.client.admincli.config.IamConfig;
import com.example.starter.iam.auth.client.IamAuthClientModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import java.io.PrintStream;
import java.util.Map;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {
      AuthModule.class,
      ConfigModule.class,
      CommandModule.class,
      IamAuthClientModule.class,
      Provider.EagerModule.class,
    })
public interface Provider {

  @Nullable Void init();

  Map<Class<?>, Object> commandByClass();

  @Named("out")
  PrintStream out();

  @Named("err")
  PrintStream err();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    @BindsInstance
    Builder iamConfig(IamConfig config);

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
