/* Licensed under Apache-2.0 2024. */
package com.example.iam.rpc.ioc;

import com.example.iam.rpc.api.IamRpcApiModule;
import com.example.iam.rpc.api.IamRpcService;
import dagger.BindsInstance;
import dagger.Component;
import io.vertx.core.Vertx;
import javax.inject.Singleton;

@Singleton
@Component(modules = {IamRpcApiModule.class})
public interface TestProvider {

  IamRpcService iamRpcService();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder vertx(Vertx vertx);

    TestProvider build();
  }
}
