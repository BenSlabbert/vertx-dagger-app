package com.example.reactivetest.dao.sql;

import static com.example.reactivetest.dao.sql.projection.ProjectionExecutor.execute;

import com.example.reactivetest.dao.sql.projection.PersonProjectionFactory;
import com.example.reactivetest.dao.sql.projection.PersonProjectionFactory.PersonProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class PersonRepository {

  private final PersonProjectionFactory personProjectionFactory;

  @Inject
  PersonRepository(PersonProjectionFactory personProjectionFactory) {
    this.personProjectionFactory = personProjectionFactory;
  }

  public Future<PersonProjection> create(SqlClient conn, String name) {
    return execute(conn, personProjectionFactory.createNextIdProjection())
        .compose(
            nextId -> {
              var projection =
                  personProjectionFactory.createInsertReturningProjection(nextId, name);

              return execute(conn, projection);
            });
  }

  public Future<List<PersonProjection>> findAll(SqlClient conn) {
    return execute(conn, personProjectionFactory.createFindPersonProjection(Integer.MAX_VALUE));
  }
}
