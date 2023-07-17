package com.example.catalog.repository;

import com.example.catalog.entity.Item;
import com.example.catalog.service.ItemService;
import io.vertx.core.Future;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {

  Future<Item> create(String name, long priceInCents);

  Future<List<String>> suggest(String name);

  Future<Page<Item>> findAll(long lastId, int size, ItemService.Direction direction);

  Future<Page<Item>> search(
      String name,
      int priceFrom,
      int priceTo,
      ItemService.Direction direction,
      long lastId,
      int size);

  Future<Optional<Item>> findById(UUID id);

  Future<Boolean> update(UUID id, String name, long priceInCents);

  Future<Boolean> delete(UUID id);
}
