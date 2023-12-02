/* Licensed under Apache-2.0 2023. */
package com.example.iam.grpc.service;

import dagger.Module;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  TokenService tokenService();

  GrpcService grpcService();
}
