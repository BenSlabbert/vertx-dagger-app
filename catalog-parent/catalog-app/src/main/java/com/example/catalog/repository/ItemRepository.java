package com.example.catalog.repository;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

  Future<Item> create(String name, long priceInCents);

  Future<List<String>> suggest(String name);

  Future<Page<Item>> findAll(int page, int size);

  Future<List<Item>> search(String name, int priceFrom, int priceTo, int page, int size);

  Future<Optional<Item>> findById(UUID id);

  Future<Boolean> update(UUID id, String name, long priceInCents);

  Future<Boolean> delete(UUID id);
}
