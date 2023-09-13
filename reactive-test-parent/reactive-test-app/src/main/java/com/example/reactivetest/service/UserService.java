package com.example.reactivetest.service;

import com.example.reactivetest.dao.sql.UserRepository;
import com.example.reactivetest.projections.UserDataObject;
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
