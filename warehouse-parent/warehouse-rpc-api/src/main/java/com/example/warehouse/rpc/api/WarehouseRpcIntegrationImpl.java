/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class WarehouseRpcIntegrationImpl implements WarehouseRpcIntegration {

  private final WarehouseRpcService warehouseRpcService;

  @Inject
  WarehouseRpcIntegrationImpl(WarehouseRpcService warehouseRpcService) {
    this.warehouseRpcService = warehouseRpcService;
  }

  @Override
  public Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(
      GetNextDeliveryJobRequestDto request) {
    return warehouseRpcService.getNextDeliveryJob(request);
  }
}
