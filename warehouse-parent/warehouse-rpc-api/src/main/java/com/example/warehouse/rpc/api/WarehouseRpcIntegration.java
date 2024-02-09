/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;

public interface WarehouseRpcIntegration {

  Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(GetNextDeliveryJobRequestDto request);
}
