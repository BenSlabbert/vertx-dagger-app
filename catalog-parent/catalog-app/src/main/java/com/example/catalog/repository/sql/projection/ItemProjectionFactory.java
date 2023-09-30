/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository.sql.projection;

import static com.example.catalog.generator.entity.generated.jooq.tables.Item.ITEM;

import com.example.catalog.projection.item.ItemProjection;
import com.example.commons.sql.Projection;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.AttachableQueryPart;
import org.jooq.DSLContext;

@Singleton
public class ItemProjectionFactory {

  private final DSLContext dsl;

  @Inject
  ItemProjectionFactory(DSLContext dsl) {
    this.dsl = dsl;
  }

  public InsertItemProjection createInsertItemProjection(String name, long priceInCents) {
    return new InsertItemProjection(name, priceInCents);
  }

  public FindItemPageProjection createFindItemPageProjection(long lastId, int size) {
    return new FindItemPageProjection(lastId, size);
  }

  public FindByIdProjection createFindByIdProjection(long id) {
    return new FindByIdProjection(id);
  }

  public UpdateProjection createUpdateProjection(
      long id, String name, long priceInCents, long version) {
    return new UpdateProjection(id, name, priceInCents, version);
  }

  public DeleteProjection createDeleteProjection(long id) {
    return new DeleteProjection(id);
  }

  public class DeleteProjection implements Projection<Void> {

    private final long id;

    private DeleteProjection(long id) {
      this.id = id;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.deleteFrom(ITEM).where(ITEM.ID.eq(id)).returning(ITEM.ID);
    }

    @Override
    public Void parse(RowSet<Row> rowSet) {
      RowIterator<Row> iterator = rowSet.iterator();

      if (!iterator.hasNext()) {
        throw new NoStackTraceException("no item for id: " + id);
      }

      return null;
    }
  }

  public class UpdateProjection implements Projection<Void> {

    private final long id;
    private final String name;
    private final long priceInCents;
    private final long version;

    private UpdateProjection(long id, String name, long priceInCents, long version) {
      this.id = id;
      this.name = name;
      this.priceInCents = priceInCents;
      this.version = version;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.update(ITEM)
          .set(ITEM.NAME, name)
          .set(ITEM.PRICE_IN_CENTS, priceInCents)
          .set(ITEM.VERSION, ITEM.VERSION.plus(1))
          .where(ITEM.ID.eq(id), ITEM.VERSION.eq(version))
          .returning(ITEM.ID);
    }

    @Override
    public Void parse(RowSet<Row> rowSet) {
      RowIterator<Row> iterator = rowSet.iterator();

      if (!iterator.hasNext()) {
        throw new NoStackTraceException("no item for id: " + id);
      }

      return null;
    }
  }

  public class FindByIdProjection implements Projection<Optional<ItemProjection>> {

    private final long id;

    private FindByIdProjection(long id) {
      this.id = id;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.select(ITEM.ID, ITEM.NAME, ITEM.PRICE_IN_CENTS, ITEM.VERSION)
          .from(ITEM)
          .where(ITEM.ID.eq(id));
    }

    @Override
    public Optional<ItemProjection> parse(RowSet<Row> rowSet) {
      RowIterator<Row> iterator = rowSet.iterator();

      if (!iterator.hasNext()) {
        return Optional.empty();
      }

      Row row = iterator.next();

      return Optional.of(ItemProjection.map(row));
    }
  }

  public class FindItemPageProjection implements Projection<List<ItemProjection>> {

    private final long lastId;
    private final int size;

    private FindItemPageProjection(long lastId, int size) {
      this.lastId = lastId;
      this.size = size;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.select(ITEM.ID, ITEM.NAME, ITEM.PRICE_IN_CENTS, ITEM.VERSION)
          .from(ITEM)
          .where(ITEM.ID.greaterThan(lastId))
          .limit(size);
    }

    @Override
    public List<ItemProjection> parse(RowSet<Row> rowSet) {
      return ItemProjection.map(rowSet);
    }
  }

  public class InsertItemProjection
      implements Projection<InsertItemProjection.CreatedItemProjection> {

    private final String name;
    private final Long priceInCents;

    private InsertItemProjection(String name, long priceInCents) {
      this.name = name;
      this.priceInCents = priceInCents;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.insertInto(ITEM)
          .columns(ITEM.NAME, ITEM.PRICE_IN_CENTS)
          .values(name, priceInCents)
          .returning(ITEM.ID, ITEM.VERSION);
    }

    @Override
    public CreatedItemProjection parse(RowSet<Row> rowSet) {
      Row row = rowSet.iterator().next();
      return new CreatedItemProjection(row.getLong(0), row.getLong(1));
    }

    public record CreatedItemProjection(long id, long version) {}
  }
}
