package com.example.catalog.repository;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;

public interface ItemRepository {

  Future<Item> create(SqlClient conn, String name, long priceInCents);

  Future<List<Item>> findAll(SqlClient conn);

  Future<Item> findById(SqlClient conn, long id);

  Future<Void> update(SqlClient conn, long id, String name, long priceInCents);

  Future<Void> delete(SqlClient conn, long id);
}
