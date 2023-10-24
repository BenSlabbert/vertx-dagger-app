/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka;

import com.example.commons.kafka.consumer.KafkaConsumerFactory;
import com.example.commons.kafka.producer.KafkaProducerFactory;
import com.example.commons.kafka.producer.TopicCreatorFactory;
import dagger.Module;

@Module(includes = {KafkaConsumerFactory.class, KafkaProducerFactory.class})
public interface KafkaModule {

  TopicCreatorFactory topicCreatorFactory();
}
