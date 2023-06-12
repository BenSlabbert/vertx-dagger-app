package com.example.catalog.repository;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

  Future<Item> create(String name, long priceInCents);

  Future<List<Item>> findAll(int from, int to);

  Future<List<Item>> searchByName(String name);

  Future<Optional<Item>> findById(UUID id);

  Future<Boolean> update(UUID id, String name, long priceInCents);

  Future<Boolean> delete(UUID id);
}
