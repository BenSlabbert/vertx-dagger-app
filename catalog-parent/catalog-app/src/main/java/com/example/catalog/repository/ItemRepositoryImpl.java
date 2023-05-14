package com.example.catalog.repository;

import static java.util.Objects.requireNonNull;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import java.util.List;
import java.util.Optional;
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
INSERT INTO item (name, price_in_cents) VALUES ($1, $2) RETURNING id
""")
        .execute(Tuple.of(name, priceInCents))
        .map(
            rows -> {
              Row row = rows.iterator().next();
              UUID id = requireNonNull(row.getUUID(0));
              return new Item(id, name, priceInCents);
            });
  }

  @Override
  public Future<List<Item>> findAll(SqlClient conn) {
    return conn.preparedQuery("""
SELECT id, name, price_in_cents
FROM item
""")
        .execute()
        .map(
            rows ->
                StreamSupport.stream(rows.spliterator(), false)
                    .map(
                        row -> {
                          int idx = 0;
                          UUID _id = requireNonNull(row.getUUID(idx++));
                          String _name = requireNonNull(row.getString(idx++));
                          long _priceInCents = requireNonNull(row.getLong(idx++));

                          return new Item(_id, _name, _priceInCents);
                        })
                    .toList());
  }

  @Override
  public Future<Optional<Item>> findById(SqlClient conn, UUID id) {
    return conn.preparedQuery("""
SELECT id, name, price_in_cents
FROM item
WHERE id=$1
""")
        .execute(Tuple.of(id))
        .map(
            rows -> {
              RowIterator<Row> iterator = rows.iterator();

              if (!iterator.hasNext()) return Optional.empty();

              Row row = iterator.next();
              int idx = 0;
              UUID _id = requireNonNull(row.getUUID(idx++));
              String _name = requireNonNull(row.getString(idx++));
              long _priceInCents = requireNonNull(row.getLong(idx++));
              return Optional.of(new Item(_id, _name, _priceInCents));
            });
  }

  @Override
  public Future<Boolean> update(SqlClient conn, UUID id, String name, long priceInCents) {
    return conn.preparedQuery("""
UPDATE item
set name=$2, price_in_cents=$3
WHERE id=$1
""")
        .execute(Tuple.of(id, name, priceInCents))
        .map(rows -> rows.rowCount() == 1);
  }

  @Override
  public Future<Boolean> delete(SqlClient conn, UUID id) {
    return conn.preparedQuery("""
DELETE
FROM item
WHERE id=$1
""")
        .execute(Tuple.of(id))
        .map(rows -> rows.rowCount() == 1);
  }
}
