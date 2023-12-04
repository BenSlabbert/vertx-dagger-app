/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@Getter
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject), access = lombok.AccessLevel.PROTECTED)
public class ServiceLifecycleManagement {

  private final Set<AutoCloseable> closeables;
}
