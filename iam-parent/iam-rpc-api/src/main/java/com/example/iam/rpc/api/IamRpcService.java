/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import com.example.iam.rpc.api.dto.CheckTokenRequestDto;
import com.example.iam.rpc.api.dto.CheckTokenResponseDto;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;

@ProxyGen // Generate service proxies
@VertxGen // Generate the handler
public interface IamRpcService {

  String ADDRESS = "RPC.IAM.TOKEN_CHECK";

  Future<CheckTokenResponseDto> check(CheckTokenRequestDto request);
}
