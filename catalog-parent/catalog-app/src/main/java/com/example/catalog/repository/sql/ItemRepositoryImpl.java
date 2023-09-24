/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository.sql;

import static com.example.commons.sql.ProjectionExecutor.execute;

import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory.InsertItemProjection.CreatedItemProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemRepositoryImpl implements ItemRepository {

  private final ItemProjectionFactory itemProjectionFactory;

  @Inject
  public ItemRepositoryImpl(ItemProjectionFactory itemProjectionFactory) {
    this.itemProjectionFactory = itemProjectionFactory;
  }

  public Future<CreatedItemProjection> create(SqlClient conn, String name, long priceInCents) {
    return execute(conn, itemProjectionFactory.createInsertItemProjection(name, priceInCents));
  }

  public Future<List<ItemProjection>> getPage(SqlClient conn, long lastId, int size) {
    return execute(conn, itemProjectionFactory.createFindItemPageProjection(lastId, size));
  }

  public Future<Optional<ItemProjection>> findById(SqlClient conn, long id) {
    return execute(conn, itemProjectionFactory.createFindByIdProjection(id));
  }

  public Future<Void> update(SqlClient conn, long id, String name, long priceInCents) {
    return execute(conn, itemProjectionFactory.createUpdateProjection(id, name, priceInCents));
  }

  public Future<Void> delete(SqlClient conn, long id) {
    return execute(conn, itemProjectionFactory.createDeleteProjection(id));
  }
}
