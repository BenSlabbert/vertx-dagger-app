/* Licensed under Apache-2.0 2023. */
package com.example.codegen.client;

import com.example.codegen.generator.annotation.AdvisedBy;

@AdvisedBy(before = true)
public class Advised {

  public final String value;

  protected Advised(String value) {
    this.value = value;
  }

  protected Advised() {
    this.value = null;
  }

  public void publicVoidMethod() {
    System.out.println("publicVoidMethod");
  }

  public String publicStringMethod() {
    System.out.println("publicStringMethod");
    return "publicStringMethod";
  }

  public void publicVoidStringMethod(String in) {
    System.out.println("publicVoidStringMethod");
  }

  public void publicVoidStringMethod(String in1, String in2) {
    System.out.println("publicVoidStringMethod");
  }

  public String publicStringMethod(String in) {
    System.out.println("publicStringMethod");
    return in;
  }

  public String publicStringMethod(String in1, String in2) {
    System.out.println("publicStringMethod");
    return in1 + in2;
  }

  private void privateVoidMethod() {
    System.out.println("privateVoidMethod");
  }
}
