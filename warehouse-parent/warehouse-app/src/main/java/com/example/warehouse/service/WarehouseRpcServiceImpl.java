/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.warehouse.generator.entity.generated.jooq.enums.DeliveryStatus;
import com.example.warehouse.repository.DeliveryProjectionFactory.FindNextDeliveryJobProjection.NextDeliveryJobProjection;
import com.example.warehouse.repository.DeliveryRepository;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.WarehouseRpcService;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.dto.GetNextDeliveryJobRequestDto;
import github.benslabbert.vertxdaggerapp.api.rpc.warehouse.dto.GetNextDeliveryJobResponseDto;
import github.benslabbert.vertxdaggercommons.transaction.reactive.TransactionBoundary;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    log.info("get next delivery: " + request.truckId());

    UUID uuid = UUID.fromString(request.truckId());

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
