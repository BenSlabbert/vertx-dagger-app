/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.repository.sql;

import static com.example.commons.sql.ProjectionExecutor.execute;

import com.example.reactivetest.projections.UserDataObject;
import com.example.reactivetest.repository.sql.projection.UserProjectionFactory;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
