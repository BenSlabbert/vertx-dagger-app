/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest;

import github.benslabbert.vertxdaggercommons.launcher.CustomApplicationHooks;
import io.vertx.launcher.application.VertxApplication;

public class ReactiveTestLauncher extends VertxApplication {

  public static void main(String[] args) {
    new ReactiveTestLauncher(args).launch();
  }

  private ReactiveTestLauncher(String[] args) {
    super(args, new CustomApplicationHooks());
  }
}
