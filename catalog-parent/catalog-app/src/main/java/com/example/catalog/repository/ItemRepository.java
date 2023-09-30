/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository;

import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory.InsertItemProjection.CreatedItemProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {

  Future<CreatedItemProjection> create(SqlClient conn, String name, long priceInCents);

  Future<List<ItemProjection>> getPage(SqlClient conn, long lastId, int size);

  Future<Optional<ItemProjection>> findById(SqlClient conn, long id);

  Future<Void> update(SqlClient conn, long id, String name, long priceInCents, long version);

  Future<Void> delete(SqlClient conn, long id);
}
