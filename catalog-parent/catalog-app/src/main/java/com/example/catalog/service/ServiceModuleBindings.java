/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import github.benslabbert.vertxdaggercommons.mesage.Consumer;

@Module
interface ServiceModuleBindings {

  @Binds
  @IntoSet
  Consumer fromCreatePaymentConsumer(CreatePaymentConsumer consumer);
}
