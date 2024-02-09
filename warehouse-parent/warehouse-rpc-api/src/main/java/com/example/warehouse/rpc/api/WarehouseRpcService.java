/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen // Generate service proxies
@VertxGen // Generate the handler
public interface WarehouseRpcService {

  String ADDRESS = "RPC.WAREHOUSE.JOBS";

  static WarehouseRpcService createClientProxy(Vertx vertx) {
    return new WarehouseRpcServiceVertxEBProxy(vertx, ADDRESS);
  }

  Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(GetNextDeliveryJobRequestDto request);
}
