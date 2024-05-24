/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository.sql;

import static github.benslabbert.vertxdaggercommons.sql.ProjectionExecutor.execute;

import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory.InsertItemProjection.CreatedItemProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemRepositoryImpl implements ItemRepository {

  private final ItemProjectionFactory itemProjectionFactory;

  @Inject
  ItemRepositoryImpl(ItemProjectionFactory itemProjectionFactory) {
    this.itemProjectionFactory = itemProjectionFactory;
  }

  public Future<CreatedItemProjection> create(SqlClient conn, String name, long priceInCents) {
    return execute(conn, itemProjectionFactory.createInsertItemProjection(name, priceInCents));
  }

  public Future<List<ItemProjection>> nextPage(SqlClient conn, long fromId, int size) {
    return execute(conn, itemProjectionFactory.createFindNextItemPageProjection(fromId, size));
  }

  @Override
  public Future<List<ItemProjection>> previousPage(SqlClient conn, long fromId, int size) {
    return execute(conn, itemProjectionFactory.createFindPreviousItemPageProjection(fromId, size))
        .map(
            items -> {
              Collections.reverse(items);
              return items;
            });
  }

  public Future<Optional<ItemProjection>> findById(SqlClient conn, long id) {
    return execute(conn, itemProjectionFactory.createFindByIdProjection(id));
  }

  public Future<Void> update(
      SqlClient conn, long id, String name, long priceInCents, long version) {
    return execute(
        conn, itemProjectionFactory.createUpdateProjection(id, name, priceInCents, version));
  }

  public Future<Void> delete(SqlClient conn, long id) {
    return execute(conn, itemProjectionFactory.createDeleteProjection(id));
  }
}
