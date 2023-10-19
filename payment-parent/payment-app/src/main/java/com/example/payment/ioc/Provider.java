/* Licensed under Apache-2.0 2023. */
package com.example.payment.ioc;

import com.example.payment.Main;
import com.example.payment.config.ConfigModule;
import com.example.payment.service.ServiceLifecycleManagement;
import com.example.payment.verticle.ApiVerticle;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {Main.class, ConfigModule.class})
public interface Provider {

  ApiVerticle provideNewApiVerticle();

  ServiceLifecycleManagement providesServiceLifecycleManagement();
}
