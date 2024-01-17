/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.service;

import com.example.iam.rpc.api.IamRpcService;
import dagger.Module;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  IamRpcService iamRpcService();
}
