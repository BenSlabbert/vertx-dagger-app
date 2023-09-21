/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.repository.sql.projection;

import static com.example.reactivetest.generator.entity.generated.jooq.tables.Outbox.OUTBOX;
import static org.jooq.conf.ParamType.INLINED;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
public class OutboxProjectionFactory {

  private final DSLContext dsl;

  @Inject
  OutboxProjectionFactory(DSLContext dsl) {
    this.dsl = dsl;
  }

  public InsertIntoOutbox create(String key, byte[] headers, byte[] body) {
    return new InsertIntoOutbox(key, headers, body);
  }

  public GetFromOutbox get(long id) {
    return new GetFromOutbox(id);
  }

  public TopFromOutbox next() {
    return new TopFromOutbox();
  }

  public DeleteFromOutbox delete(long id) {
    return new DeleteFromOutbox(id);
  }

  public class TopFromOutbox implements Projection<Optional<GetFromOutboxProjection>> {

    private TopFromOutbox() {}

    @Override
    public String getSql() {
      return dsl.select(OUTBOX.ID, OUTBOX.KEY, OUTBOX.HEADERS, OUTBOX.VALUE)
          .from(OUTBOX)
          .orderBy(OUTBOX.ID.asc())
          .limit(1)
          .getSQL(INLINED);
    }

    @Override
    public Optional<GetFromOutboxProjection> parse(RowSet<Row> rowSet) {
      RowIterator<Row> itr = rowSet.iterator();

      if (!itr.hasNext()) {
        return Optional.empty();
      }

      Row row = itr.next();
      return Optional.of(
          new GetFromOutboxProjection(
              row.getLong(0),
              row.getString(1),
              row.getBuffer(2).getBytes(),
              row.getBuffer(3).getBytes()));
    }
  }

  public class GetFromOutbox implements Projection<GetFromOutboxProjection> {

    private final long id;

    private GetFromOutbox(long id) {
      this.id = id;
    }

    @Override
    public String getSql() {
      return dsl.select(OUTBOX.ID, OUTBOX.KEY, OUTBOX.HEADERS, OUTBOX.VALUE)
          .from(OUTBOX)
          .where(OUTBOX.ID.eq(id))
          .getSQL(INLINED);
    }

    @Override
    public GetFromOutboxProjection parse(RowSet<Row> rowSet) {
      Row row = rowSet.iterator().next();
      return new GetFromOutboxProjection(
          row.getLong(0),
          row.getString(1),
          row.getBuffer(2).getBytes(),
          row.getBuffer(3).getBytes());
    }
  }

  public record GetFromOutboxProjection(long id, String key, byte[] headers, byte[] value) {

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Long l) {
        return id() == l;
      }

      return false;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(id());
    }

    @Override
    public String toString() {
      return Long.toString(id());
    }
  }

  public class DeleteFromOutbox implements Projection<DeleteFromOutbox.DeleteOutboxProjection> {

    private final long id;

    private DeleteFromOutbox(long id) {
      this.id = id;
    }

    @Override
    public String getSql() {
      return dsl.deleteFrom(OUTBOX).where(OUTBOX.ID.eq(id)).returning(OUTBOX.ID).getSQL(INLINED);
    }

    @Override
    public DeleteOutboxProjection parse(RowSet<Row> rowSet) {
      Row row = rowSet.iterator().next();
      return new DeleteOutboxProjection(row.getLong(0));
    }

    public record DeleteOutboxProjection(long id) {}
  }

  public class InsertIntoOutbox implements Projection<InsertIntoOutbox.InsertOutboxProjection> {

    private final String key;
    private final byte[] headers;
    private final byte[] body;

    private InsertIntoOutbox(String key, byte[] headers, byte[] body) {
      this.key = key;
      this.headers = headers;
      this.body = body;
    }

    @Override
    public String getSql() {
      return dsl.insertInto(OUTBOX)
          .columns(OUTBOX.KEY, OUTBOX.HEADERS, OUTBOX.VALUE)
          .values(key, headers, body)
          .returning(OUTBOX.ID)
          .getSQL(INLINED);
    }

    @Override
    public InsertOutboxProjection parse(RowSet<Row> rowSet) {
      Row row = rowSet.iterator().next();
      return new InsertOutboxProjection(row.getLong(0));
    }

    public record InsertOutboxProjection(long id) {}
  }
}
