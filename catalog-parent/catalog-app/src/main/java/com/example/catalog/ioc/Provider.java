/* Licensed under Apache-2.0 2023. */
package com.example.catalog.ioc;

import com.example.catalog.Main;
import com.example.catalog.config.ConfigModule;
import com.example.catalog.integration.IntegrationModule;
import com.example.catalog.repository.RepositoryModule;
import com.example.catalog.service.ServiceLifecycleManagement;
import com.example.catalog.verticle.ApiVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(
    modules = {RepositoryModule.class, Main.class, ConfigModule.class, IntegrationModule.class})
public interface Provider {

  ApiVerticle provideNewApiVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();
}
