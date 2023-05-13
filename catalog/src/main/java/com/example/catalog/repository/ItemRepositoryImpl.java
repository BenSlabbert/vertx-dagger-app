package com.example.catalog.repository;

import static java.util.Objects.requireNonNull;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemRepositoryImpl implements ItemRepository {

  @Inject
  ItemRepositoryImpl() {}

  @Override
  public Future<Item> create(SqlClient conn, String name, long priceInCents) {
    return conn.preparedQuery(
            """
INSERT INTO item (name, price_in_cents) VALUES ($1, $2) RETURNING id, external_ref
""")
        .execute(Tuple.of(name, priceInCents))
        .map(
            rows -> {
              Row row = rows.iterator().next();
              Long newId = requireNonNull(row.getLong(0));
              UUID uuid = requireNonNull(row.getUUID(1));
              return new Item(newId, uuid, name, priceInCents);
            });
  }

  @Override
  public Future<List<Item>> findAll(SqlClient conn) {
    return conn.preparedQuery("""
SELECT id, external_ref, name, price_in_cents
FROM item
""")
        .execute()
        .map(
            rows ->
                StreamSupport.stream(rows.spliterator(), false)
                    .map(
                        row -> {
                          Long _id = requireNonNull(row.getLong(0));
                          UUID _uuid = requireNonNull(row.getUUID(1));
                          String _name = requireNonNull(row.getString(2));
                          long _priceInCents = requireNonNull(row.getLong(3));

                          return new Item(_id, _uuid, _name, _priceInCents);
                        })
                    .toList());
  }

  @Override
  public Future<Item> findById(SqlClient conn, long id) {
    return conn.preparedQuery(
            """
SELECT id, external_ref, name, price_in_cents
FROM item
WHERE id=$1
""")
        .execute(Tuple.of(id))
        .map(
            rows -> {
              Row row = rows.iterator().next();
              Long _id = requireNonNull(row.getLong(0));
              UUID _uuid = requireNonNull(row.getUUID(1));
              String _name = requireNonNull(row.getString(2));
              long _priceInCents = requireNonNull(row.getLong(3));
              return new Item(_id, _uuid, _name, _priceInCents);
            });
  }

  @Override
  public Future<Void> update(SqlClient conn, long id, String name, long priceInCents) {
    return conn.preparedQuery("""
UPDATE item
set name=$2, price_in_cents=$3
WHERE id=$1
""")
        .execute(Tuple.of(id, name, priceInCents))
        .map(rows -> null);
  }

  @Override
  public Future<Void> delete(SqlClient conn, long id) {
    return conn.preparedQuery("""
DELETE
FROM item
WHERE id=$1
""")
        .execute(Tuple.of(id))
        .map(rows -> null);
  }
}
