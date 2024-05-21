/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.warehouse.PersistenceTest;
import com.example.warehouse.rpc.api.WarehouseRpcService;
import com.example.warehouse.rpc.api.WarehouseRpcServiceVertxEBClientProxy;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class WarehouseRpcServiceImplTest extends PersistenceTest {

  @Test
  void authTest_success(Vertx vertx, VertxTestContext testContext) {
    WarehouseRpcServiceVertxEBClientProxy service =
        new WarehouseRpcServiceVertxEBClientProxy(
            vertx,
            WarehouseRpcService.ADDRESS,
            new DeliveryOptions().addHeader("auth-token", validJwtToken));

    Future<GetNextDeliveryJobResponseDto> nextDeliveryJob =
        service.getNextDeliveryJob(
            GetNextDeliveryJobRequestDto.builder().truckId(UUID.randomUUID().toString()).build());

    nextDeliveryJob.onComplete(
        testContext.succeeding(
            response -> {
              System.err.println("response = " + response);
              assertThat(response).isNotNull();
              assertThat(response.deliveryId()).isNull();
              testContext.completeNow();
            }));
  }

  @Test
  void authTest_failure(Vertx vertx, VertxTestContext testContext) {
    WarehouseRpcServiceVertxEBClientProxy service =
        new WarehouseRpcServiceVertxEBClientProxy(
            vertx,
            WarehouseRpcService.ADDRESS,
            new DeliveryOptions().addHeader("auth-token", invalidJwtToken));

    Future<GetNextDeliveryJobResponseDto> nextDeliveryJob =
        service.getNextDeliveryJob(
            GetNextDeliveryJobRequestDto.builder().truckId(UUID.randomUUID().toString()).build());

    nextDeliveryJob.onComplete(
        testContext.failing(
            err -> {
              assertThat(err).isNotNull();
              assertThat(err).isInstanceOf(ReplyException.class);
              ReplyException replyException = (ReplyException) err;
              assertThat(replyException.failureCode()).isEqualTo(403);
              assertThat(replyException.failureType()).isEqualTo(ReplyFailure.RECIPIENT_FAILURE);
              assertThat(replyException.getMessage()).isEqualTo("Forbidden");
              testContext.completeNow();
            }));
  }

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
              assertThat(response.deliveryId()).isNull();
              testContext.completeNow();
            }));
  }
}
