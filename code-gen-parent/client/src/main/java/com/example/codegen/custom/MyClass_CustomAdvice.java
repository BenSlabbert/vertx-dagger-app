/* Licensed under Apache-2.0 2023. */
package com.example.codegen.custom;

import com.example.codegen.client.LogAdvice;
import javax.inject.Inject;
import javax.inject.Provider;

// class name is the super type suffixed by the advisor
@javax.inject.Singleton
class MyClass_CustomAdvice extends MyClass {

  // static reference to the class of the super type
  private static final Class<MyClass> clazz = MyClass.class;

  // depend on all advisors that are used
  private final Provider<CustomAdvice> customAdviceProvider;
  private final Provider<LogAdvice> logAdviceProvider;

  // inject all advisors
  @Inject
  MyClass_CustomAdvice(
      Provider<CustomAdvice> customAdviceProvider, Provider<LogAdvice> logAdviceProvider) {
    this.customAdviceProvider = customAdviceProvider;
    this.logAdviceProvider = logAdviceProvider;
  }

  @Override
  public void method() {
    // get the advisor instance
    CustomAdvice customAdvice = customAdviceProvider.get();
    // customize the advisor
    customAdvice.customize("foo", 1, true);

    // get the advisor instance
    LogAdvice logAdvice = logAdviceProvider.get();

    // invoke the advisors
    customAdvice.before(clazz, "method");
    logAdvice.before(clazz, "method");

    // invoke the super method
    super.method();

    // invoke the advisors
    customAdvice.after(clazz, "method", null);
    logAdvice.after(clazz, "method", null);
  }
}
