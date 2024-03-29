/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.commons.transaction.reactive.TransactionBoundary;
import com.example.warehouse.generator.entity.generated.jooq.enums.DeliveryStatus;
import com.example.warehouse.repository.DeliveryProjectionFactory.FindNextDeliveryJobProjection.NextDeliveryJobProjection;
import com.example.warehouse.repository.DeliveryRepository;
import com.example.warehouse.rpc.api.WarehouseRpcService;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.Pool;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class WarehouseRpcServiceImpl extends TransactionBoundary implements WarehouseRpcService {

  private static final Logger log = LoggerFactory.getLogger(WarehouseRpcServiceImpl.class);

  private final DeliveryRepository deliveryRepository;

  @Inject
  WarehouseRpcServiceImpl(Pool pool, DeliveryRepository deliveryRepository) {
    super(pool);
    this.deliveryRepository = deliveryRepository;
  }

  @Override
  public Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(
      GetNextDeliveryJobRequestDto request) {
    log.info("get next delivery: " + request.getTruckId());

    UUID uuid = UUID.fromString(request.getTruckId());

    Future<List<NextDeliveryJobProjection>> deliveries =
        doInTransaction(conn -> deliveryRepository.findNextDeliveryJob(conn, uuid));

    return deliveries.map(
        nextDeliveries -> {
          if (nextDeliveries.isEmpty()) {
            return GetNextDeliveryJobResponseDto.builder().build();
          }

          Optional<NextDeliveryJobProjection> maybeDeliveryActive =
              nextDeliveries.stream()
                  .filter(d -> DeliveryStatus.IN_TRANSIT == d.deliveryStatus())
                  .findFirst();

          if (maybeDeliveryActive.isPresent()) {
            return GetNextDeliveryJobResponseDto.builder()
                .deliveryId(maybeDeliveryActive.get().deliveryId())
                .build();
          }

          NextDeliveryJobProjection next = nextDeliveries.getFirst();
          return GetNextDeliveryJobResponseDto.builder().deliveryId(next.deliveryId()).build();
        });
  }
}
