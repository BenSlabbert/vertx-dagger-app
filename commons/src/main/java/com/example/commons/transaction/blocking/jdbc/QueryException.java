/* Licensed under Apache-2.0 2024. */
package com.example.commons.transaction.blocking.jdbc;

public class QueryException extends RuntimeException {
  QueryException(Throwable cause) {
    super(cause);
  }
}
