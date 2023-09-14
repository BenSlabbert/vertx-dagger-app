/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.dao.sql;

import static com.example.reactivetest.dao.sql.projection.ProjectionExecutor.execute;

import com.example.reactivetest.dao.sql.projection.UserProjectionFactory;
import com.example.reactivetest.projections.UserDataObject;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class UserRepository {

  private final UserProjectionFactory userProjectionFactory;

  @Inject
  public UserRepository(UserProjectionFactory userProjectionFactory) {
    this.userProjectionFactory = userProjectionFactory;
  }

  public Future<List<UserDataObject>> findAll(SqlClient conn) {
    return execute(conn, userProjectionFactory.createUserProjection());
  }
}
