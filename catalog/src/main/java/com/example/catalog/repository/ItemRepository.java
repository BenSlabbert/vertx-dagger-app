package com.example.catalog.repository;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

  Future<Item> create(SqlClient conn, String name, long priceInCents);

  Future<List<Item>> findAll(SqlClient conn);

  Future<Optional<Item>> findById(SqlClient conn, UUID id);

  Future<Boolean> update(SqlClient conn, UUID id, String name, long priceInCents);

  Future<Boolean> delete(SqlClient conn, UUID id);
}
