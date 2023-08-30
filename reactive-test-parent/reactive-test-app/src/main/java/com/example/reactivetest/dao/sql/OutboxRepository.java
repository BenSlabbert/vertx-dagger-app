package com.example.reactivetest.dao.sql;

import static com.example.reactivetest.dao.sql.projection.ProjectionExecutor.execute;

import com.example.reactivetest.dao.sql.projection.OutboxProjectionFactory;
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

  public Future<OutboxProjectionFactory.InsertIntoOutbox.InsertOutboxProjection> insert(
      SqlClient conn, String key, byte[] headers, byte[] body) {
    return execute(conn, outboxProjectionFactory.create(key, headers, body));
  }

  public Future<OutboxProjectionFactory.GetFromOutboxProjection> get(SqlClient conn, long id) {
    return execute(conn, outboxProjectionFactory.get(id));
  }

  public Future<Optional<OutboxProjectionFactory.GetFromOutboxProjection>> next(SqlClient conn) {
    return execute(conn, outboxProjectionFactory.next());
  }

  public Future<OutboxProjectionFactory.DeleteFromOutbox.DeleteOutboxProjection> delete(
      SqlClient conn, long id) {
    return execute(conn, outboxProjectionFactory.delete(id));
  }
}
