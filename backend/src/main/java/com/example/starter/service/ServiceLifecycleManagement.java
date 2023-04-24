package com.example.starter.service;

import java.util.Set;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class ServiceLifecycleManagement {

  private final Set<AutoCloseable> services;

  @Inject
  public ServiceLifecycleManagement(Set<AutoCloseable> services) {
    this.services = services;
  }

  public void close() {
    for (AutoCloseable service : services) {
      try {
        service.close();
      } catch (Exception e) {
        log.log(Level.SEVERE, "unable to close resources", e);
      }
    }
  }
}
