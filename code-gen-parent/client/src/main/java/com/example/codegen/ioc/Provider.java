/* Licensed under Apache-2.0 2023. */
package com.example.codegen.ioc;

import com.example.codegen.client.ClientModule;
import com.example.codegen.client.Example;
import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = ClientModule.class)
public interface Provider {

  Example example();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder string(String str);

    @BindsInstance
    Builder integer(int i);

    Provider build();
  }
}
