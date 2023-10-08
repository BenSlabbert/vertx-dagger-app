/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.producer;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface TopicCreatorFactory {

  TopicCreator forTopic(String topic);
}
