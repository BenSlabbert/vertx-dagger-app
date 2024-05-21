/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.rpc.api.dto.GetNextDeliveryJobResponseDto;
import github.benslabbert.vertxdaggercodegen.annotation.security.SecuredProxy;
import github.benslabbert.vertxdaggercodegen.annotation.serviceproxy.GenerateProxies;
import io.vertx.core.Future;

@SecuredProxy
@GenerateProxies
public interface WarehouseRpcService {

  String ADDRESS = "RPC.WAREHOUSE.JOBS";

  @SecuredProxy.SecuredAction(
      group = "warehouse",
      role = "worker",
      permissions = {"read"})
  Future<GetNextDeliveryJobResponseDto> getNextDeliveryJob(GetNextDeliveryJobRequestDto request);
}
