/* Licensed under Apache-2.0 2023. */
package com.example.commons.spi;

import com.example.commons.thread.VirtualThreadFactory;
import io.vertx.core.impl.VertxBuilder;
import io.vertx.core.impl.VertxThread;
import io.vertx.core.spi.VertxThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomVertxServiceProvider implements VertxThreadFactory {

  @Override
  public void init(VertxBuilder builder) {
    log.info("loading custom VertxThreadFactory service provider");

    if (builder.threadFactory() == null) {
      builder.threadFactory(this);
    } else {
      log.warn("a thread factory has already been set!");
    }
  }

  @Override
  public VertxThread newVertxThread(
      Runnable target, String name, boolean worker, long maxExecTime, TimeUnit maxExecTimeUnit) {

    if (worker) {
      return new MyVertxThread(target, name, worker, maxExecTime, maxExecTimeUnit);
    }

    // use normal implementation
    return VertxThreadFactory.super.newVertxThread(
        target, name, worker, maxExecTime, maxExecTimeUnit);
  }

  private static class MyVertxThread extends VertxThread {

    private final Thread thread;

    private MyVertxThread(
        Runnable target, String name, boolean worker, long maxExecTime, TimeUnit maxExecTimeUnit) {
      super(target, name, worker, maxExecTime, maxExecTimeUnit);
      this.thread = VirtualThreadFactory.THREAD_FACTORY.newThread(target);
    }

    // delegate thread methods to virtual thread
    @Override
    protected Object clone() throws CloneNotSupportedException {
      log.info("clone");
      return super.clone();
    }

    @Override
    public void start() {
      log.info("start");
      thread.start();
    }

    @Override
    public void run() {
      log.info("run");
      thread.start();
    }

    @Override
    public void interrupt() {
      log.info("interrupt");
      thread.interrupt();
    }

    @Override
    public boolean isInterrupted() {
      log.info("isInterrupted");
      return thread.isInterrupted();
    }

    @Override
    public int countStackFrames() {
      log.info("countStackFrames");
      return thread.countStackFrames();
    }

    @Override
    public String toString() {
      log.info("toString");
      return thread.toString();
    }

    @Override
    public ClassLoader getContextClassLoader() {
      log.info("getContextClassLoader");
      return thread.getContextClassLoader();
    }

    @Override
    public void setContextClassLoader(ClassLoader cl) {
      log.info("setContextClassLoader");
      thread.setContextClassLoader(cl);
    }

    @Override
    public StackTraceElement[] getStackTrace() {
      log.info("getStackTrace");
      return thread.getStackTrace();
    }

    @Override
    public long getId() {
      log.info("getId");
      return thread.getId();
    }

    @Override
    public State getState() {
      log.info("getState");
      return thread.getState();
    }

    @Override
    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
      log.info("getUncaughtExceptionHandler");
      return thread.getUncaughtExceptionHandler();
    }

    @Override
    public void setUncaughtExceptionHandler(UncaughtExceptionHandler ueh) {
      log.info("setUncaughtExceptionHandler");
      thread.setUncaughtExceptionHandler(ueh);
    }

    // override netty methods
    @Override
    public boolean willCleanupFastThreadLocals() {
      log.info("willCleanupFastThreadLocals");
      boolean superResult = super.willCleanupFastThreadLocals();
      log.info("willCleanupFastThreadLocals ? " + superResult);
      return superResult;
    }

    @Override
    public boolean permitBlockingCalls() {
      log.info("permitBlockingCalls");
      boolean superResult = super.permitBlockingCalls();
      log.info("willCleanupFastThreadLocals ? " + superResult);
      return superResult;
    }
  }
}
