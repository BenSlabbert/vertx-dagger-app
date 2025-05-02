/* Licensed under Apache-2.0 2023. */
package com.example.payment;

import github.benslabbert.vertxdaggercommons.launcher.CustomApplicationHooks;
import io.vertx.launcher.application.VertxApplication;

public class PaymentAppLauncher extends VertxApplication {

  static {
    System.setProperty("org.jooq.no-tips", "true");
    System.setProperty("org.jooq.no-logo", "true");
  }

  public static void main(String[] args) {
    new PaymentAppLauncher(args).launch();
  }

  private PaymentAppLauncher(String[] args) {
    super(args, new CustomApplicationHooks());
  }
}
