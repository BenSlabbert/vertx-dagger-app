/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.warehouse.rpc.api.WarehouseRpcService;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class WarehouseRpcServiceImpl implements WarehouseRpcService {

  private static final Logger log = LoggerFactory.getLogger(WarehouseRpcServiceImpl.class);

  @Inject
  WarehouseRpcServiceImpl() {}

  @Override
  public Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(
      GetNextDeliveryJobRequestDto request) {
    log.info("get next job: " + request.getTruckId());
    return Future.failedFuture("not implemented");
  }
}
