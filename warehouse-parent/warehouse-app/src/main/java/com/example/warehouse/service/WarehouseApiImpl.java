/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import com.example.warehouse.api.WarehouseApi;
import com.example.warehouse.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.core.Future;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class WarehouseApiImpl implements WarehouseApi {

  @Inject
  WarehouseApiImpl() {}

  @Override
  public Future<GetNextDeliveryJobResponseDto> login(GetNextDeliveryJobRequestDto req) {
    return Future.succeededFuture(
        GetNextDeliveryJobResponseDto.builder().jobId("new job id").build());
  }
}
