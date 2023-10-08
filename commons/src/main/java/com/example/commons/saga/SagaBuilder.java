/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import com.google.protobuf.GeneratedMessageV3;
import io.vertx.kafka.client.producer.KafkaProducer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;

public class SagaBuilder {

  private final List<SagaStage> stages = new ArrayList<>();
  private final KafkaProducer<String, GeneratedMessageV3> producer;

  @Inject
  SagaBuilder(KafkaProducer<String, GeneratedMessageV3> producer) {
    // needed for dagger to add to the dependency graph
    this.producer = producer;
  }

  private SagaStage.SagaStageBuilder stageBuilder;

  public SagaBuilder withStage() {
    stageBuilder = new SagaStage.SagaStageBuilder();
    return this;
  }

  public SagaBuilder withTopics(String commandTopic, String resultTopic) {
    stageBuilder.commandTopic(commandTopic);
    stageBuilder.resultTopic(resultTopic);
    return this;
  }

  public SagaBuilder withHandler(SagaStageHandler handler) {
    stageBuilder.handler(handler);
    stageBuilder.producer(producer);
    SagaStage stage = stageBuilder.build();
    stages.add(stage);
    stageBuilder = null;
    return this;
  }

  public SagaExecutor build() {
    if (stageBuilder != null) {
      throw new IllegalStateException("incomplete stage");
    }

    return new SagaExecutor(UUID.randomUUID().toString(), Collections.unmodifiableList(stages));
  }
}
