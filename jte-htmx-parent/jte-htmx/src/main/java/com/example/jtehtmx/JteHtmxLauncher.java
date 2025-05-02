/* Licensed under Apache-2.0 2024. */
package com.example.jtehtmx;

import github.benslabbert.vertxdaggercommons.launcher.CustomApplicationHooks;
import io.vertx.launcher.application.VertxApplication;

public class JteHtmxLauncher extends VertxApplication {

  public static void main(String[] args) {
    new JteHtmxLauncher(args).launch();
  }

  private JteHtmxLauncher(String[] args) {
    super(args, new CustomApplicationHooks());
  }
}
