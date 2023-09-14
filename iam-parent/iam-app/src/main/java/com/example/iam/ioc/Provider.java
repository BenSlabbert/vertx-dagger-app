/* Licensed under Apache-2.0 2023. */
package com.example.iam.ioc;

import com.example.iam.Main;
import com.example.iam.repository.RepositoryModule;
import com.example.iam.service.ServiceLifecycleManagement;
import com.example.iam.service.ServiceModule;
import com.example.iam.verticle.ApiVerticle;
import com.example.iam.verticle.GrpcVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {RepositoryModule.class, ServiceModule.class, Main.class})
public interface Provider {
  ApiVerticle provideNewApiVerticle();

  GrpcVerticle provideNewGrpcVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();
}
