/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Client {

  private final MyClass myClass;
  private final OnlyAdvised onlyAdvised;
  private final OnlyCustom onlyCustom;

  @Inject
  Client(MyClass myClass, OnlyAdvised onlyAdvised, OnlyCustom onlyCustom) {
    this.myClass = myClass;
    this.onlyAdvised = onlyAdvised;
    this.onlyCustom = onlyCustom;
  }

  MyClass getMyClass() {
    return myClass;
  }

  OnlyAdvised getOnlyAdvised() {
    return onlyAdvised;
  }

  OnlyCustom getOnlyCustom() {
    return onlyCustom;
  }
}
