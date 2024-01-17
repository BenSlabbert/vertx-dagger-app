/* Licensed under Apache-2.0 2023. */
package com.example.codegen.ioc;

import com.example.codegen.advice.AdviceModule;
import com.example.codegen.advice.DependencyB;
import com.example.codegen.advice.MeasureAdvice;
import com.example.codegen.client.ClientModule;
import com.example.codegen.client.Example;
import com.example.codegen.custom.Client;
import com.example.codegen.custom.CustomModule;
import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AdviceModule.class, ClientModule.class, CustomModule.class})
public interface Provider {

  Example example();

  DependencyB dependencyB();

  MeasureAdvice measureAdvice();

  Client client();

  String string();

  int integer();

  @Component.Builder
  interface Builder {

    @BindsInstance
    Builder string(String str);

    @BindsInstance
    Builder integer(int i);

    Provider build();
  }
}
