/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

public class JdbcTransactionException extends RuntimeException {
  public JdbcTransactionException(Throwable cause) {
    super(cause);
  }
}
