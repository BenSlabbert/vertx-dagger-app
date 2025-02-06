/* Licensed under Apache-2.0 2025. */
package com.example.payment.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
interface ServiceModuleBindings {

  @Binds
  @IntoSet
  Consumer fromCreatePaymentHandler(CreatePaymentHandler createPaymentHandler);
}
