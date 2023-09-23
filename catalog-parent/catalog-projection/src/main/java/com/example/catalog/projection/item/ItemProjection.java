/* Licensed under Apache-2.0 2023. */
package com.example.catalog.projection.item;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import java.util.List;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@RowMapped
@DataObject(publicConverter = false)
@NoArgsConstructor
@AllArgsConstructor
public class ItemProjection {

  @Column(name = "id")
  private long id;

  @Column(name = "name")
  private String name;

  @Column(name = "price_in_cents")
  private long priceInCents;

  public static ItemProjection map(Row row) {
    return ItemProjectionRowMapper.INSTANCE.map(row);
  }

  public static List<ItemProjection> map(RowSet<Row> rowSet) {
    return StreamSupport.stream(rowSet.spliterator(), false)
        .collect(ItemProjectionRowMapper.COLLECTOR);
  }
}
