/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import com.example.iam.rpc.api.dto.CheckTokenResponseDto;
import io.vertx.core.Future;

public interface IamRpcIntegration {

  Future<CheckTokenResponseDto> isTokenValid(String token);
}
