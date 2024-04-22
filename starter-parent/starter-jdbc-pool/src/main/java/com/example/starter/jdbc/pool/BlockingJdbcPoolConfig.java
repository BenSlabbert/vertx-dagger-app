/* Licensed under Apache-2.0 2024. */
package com.example.starter.jdbc.pool;

import static com.example.commons.thread.VirtualThreadFactory.THREAD_FACTORY;

import com.example.commons.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Module
final class BlockingJdbcPoolConfig implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(BlockingJdbcPoolConfig.class);
  private static HikariDataSource dataSource = null;

  @Inject
  BlockingJdbcPoolConfig() {}

  @Provides
  @Singleton
  static DataSource dataSource(Config config) {
    log.info("creating hikari datasource");
    Config.PostgresConfig postgres = config.postgresConfig();
    Objects.requireNonNull(postgres);
    HikariConfig cfg = getHikariConfig(postgres);
    dataSource = new HikariDataSource(cfg);

    try (var c = dataSource.getConnection()) {
      String s = c.nativeSQL("select 1");
      log.info("test connection: %s".formatted(s));
    } catch (Exception e) {
      log.error("failed to get connection", e);
      throw new NoStackTraceException(e);
    }

    return dataSource;
  }

  private static HikariConfig getHikariConfig(Config.PostgresConfig postgres) {
    HikariConfig cfg = new HikariConfig();

    cfg.setUsername(postgres.username());
    cfg.setPassword(postgres.password());
    cfg.setJdbcUrl(
        "jdbc:postgresql://%s:%d/%s"
            .formatted(postgres.host(), postgres.port(), postgres.database()));
    cfg.setThreadFactory(THREAD_FACTORY);
    cfg.setConnectionTestQuery("select 1");
    cfg.setPoolName("hikari-pool");
    cfg.setMaximumPoolSize(2);
    cfg.setAutoCommit(false);
    cfg.setConnectionTimeout(Duration.ofSeconds(5L).toMillis());

    // https://github.com/brettwooldridge/HikariCP#frequently-used
    var executor = new ScheduledThreadPoolExecutor(1, THREAD_FACTORY);
    executor.setRemoveOnCancelPolicy(true);
    cfg.setScheduledExecutor(executor);

    return cfg;
  }

  @SuppressWarnings("java:S106") // logger is not available
  @Override
  public void close() {
    if (null == dataSource) return;

    System.err.println("closing dataSource");
    dataSource.close();
  }
}
