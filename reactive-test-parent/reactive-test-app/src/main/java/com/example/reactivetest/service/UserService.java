/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import com.example.reactivetest.projections.UserDataObject;
import com.example.reactivetest.repository.sql.UserRepository;
import github.benslabbert.vertxdaggercommons.transaction.reactive.TransactionBoundary;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService extends TransactionBoundary {

  private final UserRepository userRepository;

  @Inject
  UserService(Pool pool, UserRepository userRepository) {
    super(pool);
    this.userRepository = userRepository;
  }

  public Future<List<UserDataObject>> findAll() {
    return doInTransaction(userRepository::findAll);
  }
}
