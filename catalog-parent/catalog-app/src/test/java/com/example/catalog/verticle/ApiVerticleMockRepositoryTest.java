/* Licensed under Apache-2.0 2023. */
package com.example.catalog.verticle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.catalog.MockRepositoryTest;
import com.example.catalog.projection.item.ItemProjection;
import io.vertx.core.Future;
import io.vertx.junit5.VertxTestContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ApiVerticleMockRepositoryTest extends MockRepositoryTest {

  @Test
  void getById(VertxTestContext testContext) {
    when(itemRepository.findById(any(), eq(1L)))
        .thenReturn(
            Future.succeededFuture(
                Optional.of(
                    ItemProjection.builder().id(1L).name("name").priceInCents(123L).build())));

    provider
        .itemService()
        .findById(1L)
        .onComplete(
            testContext.succeeding(
                maybeItem ->
                    testContext.verify(
                        () ->
                            assertThat(maybeItem)
                                .isPresent()
                                .get()
                                .satisfies(
                                    item -> {
                                      assertThat(item.id()).isEqualTo(1L);
                                      assertThat(item.name()).isEqualTo("name");
                                      assertThat(item.priceInCents()).isEqualTo(123L);
                                      testContext.completeNow();
                                    }))));

    verify(itemRepository).findById(any(), eq(1L));
  }
}
