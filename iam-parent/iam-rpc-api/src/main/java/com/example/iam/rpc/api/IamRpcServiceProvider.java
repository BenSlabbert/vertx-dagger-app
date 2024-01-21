/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.api;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import javax.inject.Singleton;

@Module
class IamRpcServiceProvider {

  private IamRpcServiceProvider() {}

  @Provides
  @Singleton
  static IamRpcService provideIamRpcService(Vertx vertx) {
    return IamRpcService.createClientProxy(vertx);
  }
}
