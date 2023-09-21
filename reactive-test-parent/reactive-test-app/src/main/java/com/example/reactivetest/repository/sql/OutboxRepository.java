/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.repository.sql;

import static com.example.reactivetest.repository.sql.projection.ProjectionExecutor.execute;

import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory.DeleteFromOutbox.DeleteOutboxProjection;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory.GetFromOutboxProjection;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory.InsertIntoOutbox.InsertOutboxProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OutboxRepository {

  private final OutboxProjectionFactory outboxProjectionFactory;

  @Inject
  OutboxRepository(OutboxProjectionFactory outboxProjectionFactory) {
    this.outboxProjectionFactory = outboxProjectionFactory;
  }

  public Future<InsertOutboxProjection> insert(
      SqlClient conn, String key, byte[] headers, byte[] body) {
    return execute(conn, outboxProjectionFactory.create(key, headers, body));
  }

  public Future<GetFromOutboxProjection> get(SqlClient conn, long id) {
    return execute(conn, outboxProjectionFactory.get(id));
  }

  public Future<Optional<GetFromOutboxProjection>> next(SqlClient conn) {
    return execute(conn, outboxProjectionFactory.next());
  }

  public Future<DeleteOutboxProjection> delete(SqlClient conn, long id) {
    return execute(conn, outboxProjectionFactory.delete(id));
  }
}
