/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  @Binds
  @IntoSet
  AutoCloseable kafkaProducerServiceAutoCloseable(KafkaProducerService kafkaProducerService);

  @Binds
  @IntoSet
  AutoCloseable kafkaOutboxEventListenerAutoCloseable(
      KafkaOutboxEventListener kafkaOutboxEventListener);
}
