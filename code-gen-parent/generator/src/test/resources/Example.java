package my.test;

import com.example.codegen.generator.annotation.BeforeAdvice;

@BeforeAdvice
public class Example {

  public void publicVoidMethod() {
    System.out.println("publicVoidMethod");
  }

    public String publicStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }
}

class BeforeAdvisor {

  public void before() {
    System.out.println("advice");
  }
}
