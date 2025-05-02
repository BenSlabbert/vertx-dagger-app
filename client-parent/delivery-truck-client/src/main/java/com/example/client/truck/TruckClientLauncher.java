/* Licensed under Apache-2.0 2024. */
package com.example.client.truck;

import github.benslabbert.vertxdaggercommons.launcher.CustomApplicationHooks;
import io.vertx.launcher.application.VertxApplication;

public class TruckClientLauncher extends VertxApplication {

  public static void main(String[] args) {
    new TruckClientLauncher(args).launch();
  }

  private TruckClientLauncher(String[] args) {
    super(args, new CustomApplicationHooks());
  }
}
