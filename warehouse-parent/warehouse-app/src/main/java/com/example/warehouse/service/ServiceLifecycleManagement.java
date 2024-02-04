/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.service;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ServiceLifecycleManagement {

  private final Set<AutoCloseable> closeables;

  @Inject
  ServiceLifecycleManagement(Set<AutoCloseable> closeables) {
    this.closeables = closeables;
  }

  public Set<AutoCloseable> closeables() {
    return closeables;
  }
}
