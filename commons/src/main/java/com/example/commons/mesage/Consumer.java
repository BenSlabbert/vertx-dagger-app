/* Licensed under Apache-2.0 2023. */
package com.example.commons.mesage;

import io.vertx.core.Future;

public interface Consumer {

  void register();

  Future<Void> unregister();
}
