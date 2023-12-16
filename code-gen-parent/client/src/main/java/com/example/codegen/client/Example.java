/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import com.example.codegen.advice.DependencyB;
import com.example.codegen.advice.MeasureAdvice;
import com.example.codegen.generator.annotation.Advised;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Advised(advisors = {LogAdvice.class, MeasureAdvice.class})
public class Example {

  private final String value;
  private final DependencyA dependencyA;
  private final int i;

  @Inject
  public Example(DependencyA depA, String value, int i) {
    this.dependencyA = depA;
    this.value = value;
    this.i = i;
  }

  public void publicVoidMethod() {
    System.out.println("publicVoidMethod");
  }

  public DependencyA publicStringMethod(DependencyA depA, int i, Object obj) {
    System.out.println("publicVoidMethod");
    return depA;
  }

  public List<String> returnList(String in) {
    System.out.println("publicVoidMethod");
    return List.of(in);
  }

  public Map<String, String> returnMap() {
    System.out.println("publicVoidMethod");
    return Map.of();
  }

  protected String protectedStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  DependencyB packagePrivateStringMethod(DependencyB in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  private String privateStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }
}
