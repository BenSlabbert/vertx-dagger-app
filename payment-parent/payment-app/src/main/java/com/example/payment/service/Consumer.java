/* Licensed under Apache-2.0 2025. */
package com.example.payment.service;

import io.vertx.core.Future;

public interface Consumer {

  void register();

  Future<Void> unregister();
}
