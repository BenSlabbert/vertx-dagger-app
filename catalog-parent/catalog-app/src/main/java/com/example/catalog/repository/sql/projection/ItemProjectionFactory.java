/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository.sql.projection;

import static com.example.catalog.generator.entity.generated.jooq.tables.Item.ITEM;
import static org.jooq.conf.ParamType.INLINED;

import com.example.commons.sql.Projection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import javax.inject.Inject;
import javax.inject.Singleton;
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

  public class InsertItemProjection implements Projection<InsertItemProjection.ItemProjection> {

    private final String name;
    private final Long priceInCents;

    public InsertItemProjection(String name, long priceInCents) {
      this.name = name;
      this.priceInCents = priceInCents;
    }

    @Override
    public String getSql() {
      return dsl.insertInto(ITEM)
          .columns(ITEM.NAME, ITEM.PRICE_IN_CENTS)
          .values(name, priceInCents)
          .returning(ITEM.ID)
          .getSQL(INLINED);
    }

    @Override
    public ItemProjection parse(RowSet<Row> rowSet) {
      Row row = rowSet.iterator().next();
      return new ItemProjection(row.getLong(0));
    }

    public record ItemProjection(long id) {}
  }
}
