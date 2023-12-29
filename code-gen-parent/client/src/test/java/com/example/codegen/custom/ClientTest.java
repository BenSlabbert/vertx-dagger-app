/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.codegen.ioc.DaggerTestProvider;
import com.example.codegen.ioc.TestProvider;
import org.junit.jupiter.api.Test;

class ClientTest {

  @Test
  void test() {
    TestProvider provider = DaggerTestProvider.builder().string("string").integer(1).build();
    Client client = provider.client();

    MyClass myClass = client.getMyClass();
    OnlyAdvised onlyAdvised = client.getOnlyAdvised();
    OnlyCustom onlyCustom = client.getOnlyCustom();

    assertThat(myClass).isInstanceOf(MyClass_Advised.class);
    assertThat(onlyAdvised).isInstanceOf(OnlyAdvised_Advised.class);
    assertThat(onlyCustom).isInstanceOf(OnlyCustom_Advised.class);

    myClass.method();
    onlyAdvised.method();
    onlyCustom.method();
  }
}
