package com.example.reactivetest.service;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.extern.java.Log;

@Log
@Getter
@Singleton
public class ServiceLifecycleManagement {

  private final Set<AutoCloseable> closeables;

  @Inject
  ServiceLifecycleManagement(Set<AutoCloseable> closeables) {
    this.closeables = closeables;
  }
}
