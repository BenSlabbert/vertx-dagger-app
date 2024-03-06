/* Licensed under Apache-2.0 2024. */
package com.example.plugin.openapi;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class Dependency implements SomeLogic {

  @Override
  public void implement() {
    System.err.println("implement");
  }
}
