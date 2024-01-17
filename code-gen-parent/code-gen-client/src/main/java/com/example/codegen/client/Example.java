/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import com.example.codegen.advice.DependencyB;
import com.example.codegen.advice.MeasureAdvice;
import com.example.codegen.custom.Custom;
import com.example.codegen.generator.annotation.Advised;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

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

  @Custom
  public void publicVoidMethod() {
    System.out.println("publicVoidMethod");
  }

  @Custom(foo = 100)
  public DependencyA publicStringMethod(DependencyA depA, int i, Object obj) {
    System.out.println("publicVoidMethod");
    return depA;
  }

  @Custom(param1 = "p1")
  public List<String> returnList(String in) {
    System.out.println("publicVoidMethod");
    return List.of(in);
  }

  @Custom(bar = true)
  public Map<String, String> returnMap() {
    System.out.println("publicVoidMethod");
    return Map.of();
  }

  protected String[] protectedStringArrMethod(String in) {
    System.out.println("publicVoidMethod");
    return new String[] {in};
  }

  protected String protectedStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  DependencyB packagePrivateStringMethod(DependencyB in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  public int primitiveMethod(int in) {
    System.out.println("primitiveMethod");
    return in;
  }

  public void varargs(int... in) {
    System.out.println("varargs: " + Arrays.toString(in));
  }

  private String privateStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }
}
