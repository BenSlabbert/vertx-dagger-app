/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository.sql;

import static com.example.commons.sql.ProjectionExecutor.execute;

import com.example.catalog.repository.sql.projection.ItemProjectionFactory;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory.InsertItemProjection.ItemProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemRepository {

  private final ItemProjectionFactory itemProjectionFactory;

  @Inject
  public ItemRepository(ItemProjectionFactory itemProjectionFactory) {
    this.itemProjectionFactory = itemProjectionFactory;
  }

  public Future<ItemProjection> create(SqlClient conn, String name, long priceInCents) {
    return execute(conn, itemProjectionFactory.createInsertItemProjection(name, priceInCents));
  }
}
