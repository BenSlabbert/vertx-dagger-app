/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking;

import static com.example.commons.thread.VirtualThreadFactory.THREAD_FACTORY;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

import com.example.commons.config.Config;
import com.example.commons.docker.DockerContainers;
import com.example.commons.future.FutureUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Future;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.jooq.Configuration;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.TransactionContext;
import org.jooq.TransactionProvider;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

@ExtendWith(VertxExtension.class)
class SimpleTransactionProviderTest {

  private static final Network network = Network.newNetwork();

  protected static final GenericContainer<?> postgres = DockerContainers.POSTGRES;

  static {
    System.setProperty("org.jooq.no-tips", "true");
    System.setProperty("org.jooq.no-logo", "true");
    postgres.start();
  }

  static {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private SimpleTransactionProvider transactionProvider;
  private HikariDataSource dataSource;
  private HikariConfig hikariConfig;
  private DSLContext dslContext;

  @BeforeEach
  void before() {
    // create table person (id serial8 primary key, name text);
    Config.PostgresConfig psqlCfg =
        Config.PostgresConfig.builder()
            .host("127.0.0.1")
            .port(postgres.getMappedPort(5432))
            .password("postgres")
            .username("postgres")
            .database("postgres")
            .build();

    hikariConfig = getHikariConfig(psqlCfg);
    dataSource = new HikariDataSource(hikariConfig);
    transactionProvider = new SimpleTransactionProvider(dataSource);
    dslContext = getDslContext();

    try (var conn = dataSource.getConnection()) {
      Statement statement = conn.createStatement();
      statement.execute("create table if not exists person(id serial8 primary key, name text)");
      conn.commit();
    } catch (Exception e) {
      fail("should create table successfully", e);
    }
  }

  private HikariConfig getHikariConfig(Config.PostgresConfig postgres) {
    HikariConfig cfg = new HikariConfig();

    cfg.setUsername(postgres.username());
    cfg.setPassword(postgres.password());
    cfg.setJdbcUrl(
        "jdbc:postgresql://%s:%d/%s"
            .formatted(postgres.host(), postgres.port(), postgres.database()));
    cfg.setThreadFactory(THREAD_FACTORY);
    cfg.setConnectionTestQuery("select 1");
    cfg.setPoolName("hikari-pool");
    cfg.setMaximumPoolSize(10);
    cfg.setAutoCommit(false);
    cfg.setConnectionTimeout(Duration.ofSeconds(5L).toMillis());
    cfg.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

    // https://github.com/brettwooldridge/HikariCP#frequently-used
    var executor = new ScheduledThreadPoolExecutor(1, THREAD_FACTORY);
    executor.setRemoveOnCancelPolicy(true);
    cfg.setScheduledExecutor(executor);

    return cfg;
  }

  private DSLContext getDslContext() {
    Configuration configuration =
        new DefaultConfiguration()
            .set(SQLDialect.POSTGRES)
            .set((TransactionProvider) transactionProvider)
            .set((ConnectionProvider) transactionProvider)
            .set(Clock.systemUTC())
            .set(FutureUtil.EXECUTOR)
            .set(new Settings().withFetchSize(5));

    return DSL.using(configuration);
  }

  @Test
  void test() {
    // SimpleTransactionProvider uses thread locals, so now we can set these from anywhere, right?
    // yes, now we can use SimpleTransactionProvider to manage the transactions
    // and have a global DSLContext
    try {
      // in the real world, not sure how to create this TransactionContext...
      // we can create something similar for the jdbc connection as well...
      transactionProvider.begin(Mockito.mock(TransactionContext.class));
      dslContext.configuration();
      dslContext.settings();

      List<String> names =
          dslContext.fetch("select name from person").map(r -> r.get("name", String.class));
      System.err.println("names: " + names);
      transactionProvider.commit(Mockito.mock(TransactionContext.class));
    } catch (Exception e) {
      transactionProvider.rollback(Mockito.mock(TransactionContext.class));
    }
  }

  @Test
  void runtimeException() {
    assertThatThrownBy(
            () ->
                dslContext.transaction(
                    tx -> {
                      throw new RuntimeException("planned");
                    }))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("planned");
  }

  @Test
  void throwable() {
    assertThatThrownBy(
            () ->
                dslContext.transaction(
                    tx -> {
                      throw new Throwable("planned");
                    }))
        .isInstanceOf(Throwable.class)
        .rootCause()
        .hasMessage("planned");
  }

  @Test
  void concurrentTest(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(3);
    Semaphore semaphore = new Semaphore(1, true);

    Future<Void> f1 = FutureUtil.run(() -> run(1, dslContext, semaphore, checkpoint));
    Future<Void> f2 = FutureUtil.run(() -> run(2, dslContext, semaphore, checkpoint));

    Future.all(f1, f2)
        .onComplete(
            testContext.succeeding(
                v -> {
                  System.err.println("all done");
                  List<String> names =
                      dslContext
                          .fetch("SELECT name from person where name like 'blah5%'")
                          .map(r -> r.get("name", String.class));
                  System.err.println("names: " + names);
                  checkpoint.flag();
                }));
  }

  void run(int id, DSLContext dslContext, Semaphore semaphore, Checkpoint checkpoint) {
    dslContext.transaction(
        tx -> {
          for (int i = 0; i < 2; i++) {
            System.err.println(id + ": get semaphor");
            semaphore.acquire();

            List<String> names =
                tx.dsl()
                    .fetch("SELECT name from person where name = 'blah5" + id + "'")
                    .map(r -> r.get("name", String.class));
            System.err.println(id + ": names before insert: " + names);

            int execute = tx.dsl().execute("INSERT INTO person (name) VALUES ('blah5" + id + "')");
            System.err.println(id + ": inserted: " + execute);

            names =
                tx.dsl()
                    .fetch("SELECT name from person where name = 'blah5" + id + "'")
                    .map(r -> r.get("name", String.class));
            System.err.println(id + ": names after insert: " + names);

            System.err.println(id + ": release semaphor");
            semaphore.release();
          }
          checkpoint.flag();
        });
  }

  @Test
  void test(VertxTestContext testContext) {
    Checkpoint checkpoint = testContext.checkpoint(2);

    int execute = dslContext.execute("SELECT 1");
    System.err.println("init dsl context: executed select 1: " + execute);

    List<Future<Void>> list = Stream.generate(() -> getRun(dslContext)).limit(1_000).toList();

    Future.all(list)
        .onComplete(
            testContext.succeeding(
                v -> {
                  System.err.println("all done");
                  checkpoint.flag();
                }));

    System.err.println("stream test");
    try (var s =
        dslContext.fetchStream("SELECT name from person").map(r -> r.get("name", String.class))) {
      AtomicInteger cnt = new AtomicInteger(0);
      s.forEach(
          name -> {
            System.err.println(name);
            cnt.incrementAndGet();
          });

      System.err.println("streamed " + cnt.get() + " records");
      checkpoint.flag();
    }
  }

  private static Future<Void> getRun(DSLContext dslContext) {
    return FutureUtil.run(
        () ->
            dslContext.transaction(
                tx -> {
                  List<String> names1 =
                      tx.dsl()
                          .fetch("SELECT name from person")
                          .map(r -> r.get("name", String.class));
                  System.err.println(names1);

                  List<String> names2 =
                      tx.dsl()
                          .fetch("SELECT name from person")
                          .map(r -> r.get("name", String.class));
                  System.err.println(names2);
                }));
  }
}
