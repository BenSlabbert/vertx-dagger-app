/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import com.example.iam.rpc.api.dto.CheckTokenRequestDto;
import com.example.iam.rpc.api.dto.CheckTokenResponseDto;
import github.benslabbert.vertxdaggercodegen.annotation.serviceproxy.GenerateProxies;
import io.vertx.core.Future;

@GenerateProxies
public interface IamRpcService {

  String ADDRESS = "RPC.IAM.TOKEN_CHECK";

  Future<CheckTokenResponseDto> check(CheckTokenRequestDto request);
}
