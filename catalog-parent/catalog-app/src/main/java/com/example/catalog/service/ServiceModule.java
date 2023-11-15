/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.commons.mesage.Consumer;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  @Binds
  @IntoSet
  Consumer fromCreatePaymentConsumer(CreatePaymentConsumer consumer);
}
