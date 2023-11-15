/* Licensed under Apache-2.0 2023. */
package com.example.commons.thread;

import java.util.concurrent.ThreadFactory;

public final class VirtualThreadFactory {

  private VirtualThreadFactory() {}

  public static final ThreadFactory THREAD_FACTORY =
      Thread.ofVirtual().name("vthread-", 1L).factory();
}
