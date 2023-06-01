package com.example.shop.ioc;

import com.example.shop.Main;
import com.example.shop.service.ServiceLifecycleManagement;
import com.example.shop.service.ServiceModule;
import com.example.shop.verticle.ConsumerVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class, Main.class})
public interface Provider {

  ConsumerVerticle provideNewConsumerVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();
}
