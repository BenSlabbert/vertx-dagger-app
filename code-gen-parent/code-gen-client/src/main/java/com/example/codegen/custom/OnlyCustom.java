/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.generator.advice.annotation.Advised;
import java.util.Map;
import javax.inject.Inject;

@Advised
class OnlyCustom {

  @Inject
  OnlyCustom() {}

  @MyAdvisor
  public Map<String, Integer> method() {
    return Map.of();
  }
}
