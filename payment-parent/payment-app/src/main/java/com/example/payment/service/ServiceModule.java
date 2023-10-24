/* Licensed under Apache-2.0 2023. */
package com.example.payment.service;

import com.example.commons.kafka.consumer.MessageHandler;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;

@Module
public interface ServiceModule {

  @Binds
  @IntoSet
  AutoCloseable asAutoCloseableKafkaService(KafkaConsumerService closeablePool);

  @Binds
  @IntoSet
  MessageHandler asMessageHandlerExampleMessageHandler(ExampleMessageHandler exampleMessageHandler);
}
