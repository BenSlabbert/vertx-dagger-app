package com.example.commons.transaction.blocking.jdbc;

public class JdbcTransactionException extends RuntimeException{

  public JdbcTransactionException(Throwable cause) {
    super(cause);
  }
}
