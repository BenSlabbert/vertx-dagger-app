/* Licensed under Apache-2.0 2023. */
package com.example.catalog.projection.item;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxdaggercodegen.annotation.projection.Column;
import github.benslabbert.vertxdaggercodegen.annotation.projection.ReactiveProjection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.stream.StreamSupport;

@ReactiveProjection
public record ItemProjection(
    @Column(name = "id") long id,
    @Column(name = "name") String name,
    @Column(name = "price_in_cents") long priceInCents,
    @Column(name = "version") long version) {

  public static ItemProjection map(Row row) {
    return ItemProjection_ReactiveRowMapper.INSTANCE.map(row);
  }

  public static List<ItemProjection> map(RowSet<Row> rowSet) {
    return StreamSupport.stream(rowSet.spliterator(), false)
        .collect(ItemProjection_ReactiveRowMapper.COLLECTOR);
  }

  public static Builder builder() {
    return new AutoBuilder_ItemProjection_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder id(long id);

    Builder name(String name);

    Builder priceInCents(long priceInCents);

    Builder version(long version);

    ItemProjection build();
  }
}
