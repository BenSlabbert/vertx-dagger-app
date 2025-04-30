/* Licensed under Apache-2.0 2023. */
package com.example.catalog;

import github.benslabbert.vertxdaggercommons.launcher.CustomApplicationHooks;
import io.vertx.launcher.application.VertxApplication;

public class CatalogAppLauncher extends VertxApplication {

  public static void main(String[] args) {
    new CatalogAppLauncher(args).launch();
  }

  private CatalogAppLauncher(String[] args) {
    super(args, new CustomApplicationHooks());
  }
}
