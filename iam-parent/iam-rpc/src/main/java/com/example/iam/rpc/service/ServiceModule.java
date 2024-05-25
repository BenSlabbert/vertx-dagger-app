/* Licensed under Apache-2.0 2023. */
package com.example.iam.rpc.service;

import dagger.Module;
import github.benslabbert.vertxdaggerapp.api.rpc.iam.IamRpcService;

@Module(includes = ServiceModuleBindings.class)
public interface ServiceModule {

  IamRpcService iamRpcService();
}
