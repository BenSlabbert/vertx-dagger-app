package my.test;

import java.util.List;
import com.example.codegen.generator.annotation.Advice;
import com.example.codegen.generator.annotation.Advised;

@Advised(advisors = {LogAdvice.class, MeasureAdvice.class})
public class AdviceTest {

  private final String value;

  AdviceTest(String value) {
    this.value = value;
  }

  public void publicVoidMethod() {
    System.out.println("publicVoidMethod");
  }

  public String publicStringMethod(String in, int i, Object obj) {
    System.out.println("publicVoidMethod");
    return in;
  }

  public List<String> returnList(String in) {
    System.out.println("publicVoidMethod");
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
