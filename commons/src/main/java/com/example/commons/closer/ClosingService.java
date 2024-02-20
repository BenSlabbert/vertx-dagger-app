/* Licensed under Apache-2.0 2024. */
package com.example.commons.closer;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ClosingService {

  private final Set<AutoCloseable> closeables;

  @Inject
  ClosingService(Set<AutoCloseable> closeables) {
    this.closeables = closeables;
  }

  public Set<AutoCloseable> closeables() {
    return closeables;
  }
}
