/* Licensed under Apache-2.0 2023. */
package com.example.codegen;

import com.example.codegen.ioc.DaggerProvider;
import com.example.codegen.ioc.Provider;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

public class Main {

  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    Provider provider = DaggerProvider.builder().string("str").integer(1).build();

    String string = provider.string();
    int integer = provider.integer();

    log.info("string: " + string);
    log.info("integer: " + integer);
  }
}
