package com.example.catalog.ioc;

import com.example.catalog.Main;
import com.example.catalog.verticle.ApiVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {Main.class})
public interface Provider {
  ApiVerticle provideNewApiVerticle();
}
