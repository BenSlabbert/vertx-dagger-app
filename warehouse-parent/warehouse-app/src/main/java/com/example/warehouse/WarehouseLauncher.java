/* Licensed under Apache-2.0 2024. */
package com.example.warehouse;

import github.benslabbert.vertxdaggercommons.launcher.CustomApplicationHooks;
import io.vertx.launcher.application.VertxApplication;

public class WarehouseLauncher extends VertxApplication {

  public static void main(String[] args) {
    new WarehouseLauncher(args).launch();
  }

  private WarehouseLauncher(String[] args) {
    super(args, new CustomApplicationHooks());
  }
}
