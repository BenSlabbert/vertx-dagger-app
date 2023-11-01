/* Licensed under Apache-2.0 2023. */
package com.example.payment.config;

import com.example.commons.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.impl.NoStackTraceException;
import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import lombok.extern.java.Log;

@Log
@Module
public class BlockingJdbcPoolConfig implements AutoCloseable {

  private static HikariDataSource dataSource = null;

  @Inject
  BlockingJdbcPoolConfig() {}

  @Provides
  @Singleton
  static DataSource dataSource(Config.PostgresConfig config) {
    log.info("creating hikari datasource");
    HikariConfig cfg = new HikariConfig();

    cfg.setUsername(config.username());
    cfg.setPassword(config.password());
    cfg.setJdbcUrl(
        "jdbc:postgresql://%s:%d/%s".formatted(config.host(), config.port(), config.database()));
    cfg.setThreadFactory(Thread.ofVirtual().factory());
    cfg.setConnectionTestQuery("select 1");
    cfg.setPoolName("payment-pool");
    cfg.setMaximumPoolSize(2);
    cfg.setAutoCommit(false);
    cfg.setConnectionTimeout(Duration.ofSeconds(5L).toMillis());

    // https://github.com/brettwooldridge/HikariCP#frequently-used
    ScheduledThreadPoolExecutor executor =
        new ScheduledThreadPoolExecutor(1, Thread.ofVirtual().factory());
    executor.setRemoveOnCancelPolicy(true);

    cfg.setScheduledExecutor(executor);

    dataSource = new HikariDataSource(cfg);

    try (var c = dataSource.getConnection()) {
      // ensure we can get a connection
    } catch (Exception e) {
      log.log(Level.SEVERE, "failed to get connection", e);
      throw new NoStackTraceException(e);
    }

    return dataSource;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() {
    if (null == dataSource) return;

    dataSource.close();
  }
}
