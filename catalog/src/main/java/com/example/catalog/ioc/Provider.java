package com.example.catalog.ioc;

import com.example.catalog.Main;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceModule;
import com.example.catalog.verticle.ApiVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, RepositoryModule.class, Main.class})
public interface Provider {
  ApiVerticle provideNewApiVerticle();
}
