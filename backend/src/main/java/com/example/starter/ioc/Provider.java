package com.example.starter.ioc;

import com.example.starter.Main;
import com.example.starter.repository.RepositoryModule;
import com.example.starter.service.ServiceLifecycleManagement;
import com.example.starter.service.ServiceModule;
import com.example.starter.verticle.ApiVerticle;
import com.example.starter.verticle.GrpcVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {RepositoryModule.class, ServiceModule.class, Main.class})
public interface Provider {
  ApiVerticle provideNewApiVerticle();

  GrpcVerticle provideNewGrpcVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();
}
