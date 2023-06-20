package com.example.catalog.repository;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

  Future<Item> create(String name, long priceInCents);

  Future<List<Item>> findAll(Integer from, Integer to);

  Future<List<Item>> search(
      String name, Integer priceFrom, Integer priceTo, Integer from, Integer to);

  Future<Optional<Item>> findById(UUID id);

  Future<Boolean> update(UUID id, String name, long priceInCents);

  Future<Boolean> delete(UUID id);
}
