/* Licensed under Apache-2.0 2023. */
package com.example.commons.saga;

import io.vertx.core.Future;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import lombok.extern.java.Log;

@Log
public class Saga {

  private final String id;
  private final List<SagaStage> stages;

  private Saga(String id, List<SagaStage> stages) {
    this.id = id;
    this.stages = stages;
  }

  public Future<String> execute() {
    ListIterator<SagaStage> iterator = stages.listIterator();

    while (iterator.hasNext()) {
      SagaStage stage = iterator.next();
      stage.sendCommand();

      boolean success = stage.getResult();

      if (success) continue;

      while (iterator.hasPrevious()) {
        stage = iterator.previous();
        stage.sendRollbackCommand();
      }
      return Future.failedFuture("%s saga failed".formatted(id));
    }

    // empty saga
    return Future.succeededFuture(id);
  }

  public static SagaBuilder builder() {
    return new SagaBuilder();
  }

  public static class SagaBuilder {

    private final String sagaId = UUID.randomUUID().toString();
    private final List<SagaStage> stages = new ArrayList<>();
    private SagaStage.SagaStageBuilder stageBuilder;

    public SagaBuilder withStage() {
      stageBuilder = new SagaStage.SagaStageBuilder().sagaId(sagaId);
      return this;
    }

    public SagaBuilder withTopics(String commandTopic, String resultTopic) {
      stageBuilder.commandTopic(commandTopic);
      stageBuilder.resultTopic(resultTopic);
      return this;
    }

    public SagaBuilder withHandler(SagaStageHandler handler) {
      stageBuilder.handler(handler);
      SagaStage stage = stageBuilder.build();
      stages.add(stage);
      stageBuilder = null;
      return this;
    }

    public Saga build() {
      if (stageBuilder != null) {
        throw new IllegalStateException("incomplete stage");
      }
      return new Saga(sagaId, Collections.unmodifiableList(stages));
    }
  }
}
