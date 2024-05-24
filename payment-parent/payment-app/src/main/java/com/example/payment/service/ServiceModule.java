/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import github.benslabbert.vertxdaggercommons.mesage.Consumer;

@Module
public interface ServiceModule {

  @Binds
  @IntoSet
  Consumer fromCreatePaymentHandler(CreatePaymentHandler createPaymentHandler);
}
