package com.example.reactivetest.ioc;

import com.example.reactivetest.Main;
import com.example.reactivetest.config.ConfigModule;
import com.example.reactivetest.service.KafkaOutboxEventListener;
import com.example.reactivetest.service.ServiceLifecycleManagement;
import com.example.reactivetest.service.ServiceModule;
import com.example.reactivetest.verticle.ApplicationVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {Main.class, ConfigModule.class, ServiceModule.class})
public interface Provider {

  ApplicationVerticle provideNewApplicationVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();

  KafkaOutboxEventListener providesKafkaOutboxEventListener();
}
