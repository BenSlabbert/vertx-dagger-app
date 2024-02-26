/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.warehouse.PersistenceTest;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class WarehouseRpcServiceImplTest extends PersistenceTest {

  @Test
  void test(VertxTestContext testContext) {
    Future<GetNextDeliveryJobResponseDto> nextDeliveryJob =
        provider
            .warehouseRpcService()
            .getNextDeliveryJob(
                GetNextDeliveryJobRequestDto.builder()
                    .truckId(UUID.randomUUID().toString())
                    .build());

    nextDeliveryJob.onComplete(
        testContext.succeeding(
            response -> {
              assertThat(response).isNotNull();
              assertThat(response.getDeliveryId()).isNull();
              testContext.completeNow();
            }));
  }
}
