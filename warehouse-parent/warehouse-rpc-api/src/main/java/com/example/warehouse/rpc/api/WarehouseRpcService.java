/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

@ProxyGen // Generate service proxies
@VertxGen // Generate the handler
public interface WarehouseRpcService {

  String ADDRESS = "RPC.WAREHOUSE.JOBS";

  Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(GetNextDeliveryJobRequestDto request);
}
