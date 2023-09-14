/* Licensed under Apache-2.0 2023. */
package com.example.shop.service;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
@Singleton
public class ServiceLifecycleManagement {

  @Getter private final Set<AutoCloseable> closeables;

  @Inject
  public ServiceLifecycleManagement(Set<AutoCloseable> closeables) {
    this.closeables = closeables;
  }
}
