package com.example.reactivetest.service;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseable(KafkaProducerService kafkaProducerService);
}
