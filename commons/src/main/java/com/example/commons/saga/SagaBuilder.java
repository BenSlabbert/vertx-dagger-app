/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import io.vertx.core.Vertx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SagaBuilder {

  private final List<SagaStage> stages = new ArrayList<>();
  private final Vertx vertx;

  @Inject
  SagaBuilder(Vertx vertx) {
    this.vertx = vertx;
  }

  private SagaStage.SagaStageBuilder stageBuilder = new SagaStage.SagaStageBuilder();

  public SagaBuilder withStage() {
    stageBuilder = new SagaStage.SagaStageBuilder();
    return this;
  }

  public SagaBuilder withCommandAddress(String address) {
    stageBuilder.commandAddress(address);
    return this;
  }

  public SagaBuilder withHandler(SagaStageHandler handler) {
    stageBuilder.handler(handler);
    stageBuilder.eventBus(vertx.eventBus());
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
