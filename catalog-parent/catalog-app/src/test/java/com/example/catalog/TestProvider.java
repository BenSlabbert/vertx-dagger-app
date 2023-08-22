package com.example.catalog;

import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceModule;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;

public abstract class TestProvider {

  protected final TestComponent testComponent;

  public TestProvider() {
    this.testComponent = DaggerTestProvider_TestComponent.builder().build();
  }

  @Singleton
  @Component(modules = {ServiceModule.class, RepositoryModule.class, FakeHandlerModule.class})
  public interface TestComponent {
    Supplier<Handler<RoutingContext>> fakeAuthHandlerSupplier();
  }

  @Module
  interface FakeHandlerModule {

    @Binds
    Supplier<Handler<RoutingContext>> createAuthHandler(
        FakeAuthHandlerSupplier fakeAuthHandlerSupplier);
  }

  @Singleton
  static class FakeAuthHandlerSupplier implements Supplier<Handler<RoutingContext>> {

    @Inject
    public FakeAuthHandlerSupplier() {}

    @Override
    public Handler<RoutingContext> get() {
      return null;
    }
  }
}
