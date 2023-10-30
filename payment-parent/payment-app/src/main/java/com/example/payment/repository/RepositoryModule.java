/* Licensed under Apache-2.0 2023. */
package com.example.payment.repository;

import dagger.Binds;
import dagger.Module;

@Module
public interface RepositoryModule {

  @Binds
  PaymentRepository paymentRepository(PaymentRepositoryImpl paymentRepositoryImpl);
}
