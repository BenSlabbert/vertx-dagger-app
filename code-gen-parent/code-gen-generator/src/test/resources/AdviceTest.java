/* Licensed under Apache-2.0 2024. */
package my.test;

import com.example.codegen.annotation.advice.Advice;
import com.example.codegen.annotation.advice.Advised;
import java.util.List;
import java.util.Map;

@Advised(advisors = {LogAdvice.class, MeasureAdvice.class})
public class AdviceTest {

  private final String value;

  AdviceTest(String value) {
    this.value = value;
  }

  public void publicVoidMethod() {
    System.out.println("publicVoidMethod");
  }

  public int publicIntMethod() {
    System.out.println("publicIntMethod");
    return 1;
  }

  public int[] publicIntArrayMethod() {
    System.out.println("publicIntArrayMethod");
    return new int[] {1, 2, 3};
  }

  public String[] publicStringArrayMethod() {
    System.out.println("publicIntArrayMethod");
    return new String[] {};
  }

  public B[] publicBArrayMethod() {
    System.out.println("publicBArrayMethod");
    return new B[] {};
  }

  public A publicAMethod() {
    System.out.println("publicAMethod");
    return new B();
  }

  public B publicBMethod() {
    System.out.println("publicBMethod");
    return new B();
  }

  public String publicStringMethod(String in, int i, Object obj) {
    System.out.println("publicVoidMethod");
    return in;
  }

  public List<String> returnList(String in) {
    System.out.println("publicVoidMethod");
    return List.of(in);
  }

  public Map<String, ? extends CharSequence> returnMapExtends() {
    System.out.println("returnMap");
    return Map.of();
  }

  public Map<String, ? super CharSequence> returnMapSuper() {
    System.out.println("returnMap");
    return Map.of();
  }

  public Map<? extends String, ? super CharSequence> returnMapSuperExtends() {
    System.out.println("returnMap");
    return Map.of();
  }

  public List<? extends String> returnListGenericExtends(String in) {
    System.out.println("returnListGenericExtends");
    return List.of(in);
  }

  public List<? super String> returnListGenericSuper(String in) {
    System.out.println("returnListGenericSuper");
    return List.of(in);
  }

  public List<?> returnListGenericWildcard(String in) {
    System.out.println("returnListGenericWildcard");
    return List.of(in);
  }

  public List returnListGenericRaw(String in) {
    System.out.println("returnListGenericRaw");
    return List.of(in);
  }

  protected String protectedStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  String packagePrivateStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  private String privateStringMethod(String in) {
    System.out.println("publicVoidMethod");
    return in;
  }

  interface A {}

  class B implements A {}
}

class LogAdvice implements Advice {

  public LogAdvice() {}

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {
    System.out.println("before " + clazz.getName() + "." + methodName);
  }

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {
    System.out.println("after " + clazz.getName() + "." + methodName);
  }
}

class MeasureAdvice implements Advice {

  public MeasureAdvice() {}

  @Override
  public void before(Class<?> clazz, String methodName, Object... args) {
    System.out.println("before " + clazz.getName() + "." + methodName);
  }

  @Override
  public void after(Class<?> clazz, String methodName, Object result) {
    System.out.println("after " + clazz.getName() + "." + methodName);
  }
}
