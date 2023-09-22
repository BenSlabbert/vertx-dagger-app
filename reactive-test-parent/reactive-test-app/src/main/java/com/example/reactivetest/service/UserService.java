/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import com.example.commons.transaction.TransactionBoundary;
import com.example.reactivetest.projections.UserDataObject;
import com.example.reactivetest.repository.sql.UserRepository;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserService extends TransactionBoundary {

  private final UserRepository userRepository;

  @Inject
  UserService(PgPool pool, UserRepository userRepository) {
    super(pool);
    this.userRepository = userRepository;
  }

  public Future<List<UserDataObject>> findAll() {
    return doInTransaction(userRepository::findAll);
  }
}
