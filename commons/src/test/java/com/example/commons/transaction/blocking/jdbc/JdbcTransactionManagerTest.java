/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTransactionManagerTest {

  private final DataSource dataSource = mock(DataSource.class);
  private JdbcTransactionManager txManager;

  @BeforeEach
  void init() {
    txManager = new JdbcTransactionManager(dataSource);
  }

  @AfterEach
  void tearDown() {
    txManager = null;
    reset(dataSource);
  }

  @Test
  void getConnection_throws() {
    assertThatThrownBy(() -> txManager.getConnection())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("cannot get connection: no transaction started");

    verifyNoInteractions(dataSource);
  }

  @Test
  void ensureActive_throws() {
    assertThatThrownBy(() -> txManager.ensureActive())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("no transaction in progress");

    verifyNoInteractions(dataSource);
  }

  @Test
  void getConnection() throws Exception {
    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);

    txManager.begin();
    Connection conn = txManager.getConnection();
    assertThat(conn).isSameAs(connection);

    txManager.ensureActive();
    txManager.commit();

    verify(dataSource).getConnection();
    verify(connection).close();
  }

  @Test
  void begin_commit() throws Exception {
    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);

    txManager.begin();
    txManager.commit();

    verify(dataSource).getConnection();
    verify(connection).close();
  }

  @Test
  void begin_rollback() throws Exception {
    Connection connection = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(connection);

    txManager.begin();
    txManager.rollback();
    verify(connection).close();
  }

  @Test
  void begin_commit_stackedTransactions() throws Exception {
    Connection conn1 = mock(Connection.class);
    Connection conn2 = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(conn1).thenReturn(conn2);

    txManager.begin();
    txManager.begin();

    txManager.commit();
    txManager.commit();

    verify(dataSource, times(2)).getConnection();

    verify(conn1).close();
    verify(conn2).close();
  }

  @Test
  void begin_rollback_stackedTransactions() throws Exception {
    Connection conn1 = mock(Connection.class);
    Connection conn2 = mock(Connection.class);
    when(dataSource.getConnection()).thenReturn(conn1).thenReturn(conn2);

    txManager.begin();
    txManager.begin();

    txManager.rollback();
    txManager.rollback();

    verify(conn1).close();
    verify(conn2).close();
  }
}
