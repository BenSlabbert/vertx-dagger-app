/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.api;

import com.example.warehouse.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;

public interface WarehouseApi {

  Future<GetNextDeliveryJobResponseDto> login(GetNextDeliveryJobRequestDto req);
}
