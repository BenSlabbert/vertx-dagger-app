/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.reactivetest.repository.sql.OutboxRepository;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.Optional;
import javax.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class KafkaOutboxServiceTest {

  private TestComponent testComponent;
  private OutboxRepository mockOutboxRepository;

  @BeforeEach
  void before() {
    testComponent = DaggerKafkaOutboxServiceTest_TestComponent.create();
    mockOutboxRepository = testComponent.testModule().mockOutboxRepository;
  }

  @Test
  void next(VertxTestContext testContext) {
    when(mockOutboxRepository.next(null)).thenReturn(Future.succeededFuture(Optional.empty()));

    testComponent
        .kafkaOutboxService()
        .next(null)
        .onFailure(testContext::failNow)
        .onSuccess(
            maybeProjection ->
                testContext.verify(
                    () -> {
                      assertThat(maybeProjection).isEmpty();

                      verify(mockOutboxRepository).next(null);
                      testContext.completeNow();
                    }));
  }

  @Singleton
  @Component(modules = {TestComponent.TestModule.class})
  interface TestComponent {

    KafkaOutboxService kafkaOutboxService();

    TestModule testModule();

    @Module
    class TestModule {

      OutboxRepository mockOutboxRepository = mock(OutboxRepository.class);

      @Provides
      OutboxRepository outboxRepository() {
        return mockOutboxRepository;
      }

      @Provides
      TestModule testModule() {
        return this;
      }
    }
  }
}
