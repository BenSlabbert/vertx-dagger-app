/* Licensed under Apache-2.0 2023. */
package com.example.catalog.repository;

import io.vertx.core.Future;
import java.util.List;

public interface SuggestionService {

  Future<Void> create(String name);

  Future<List<String>> suggest(String name);

  Future<Void> update(String oldName, String newName);

  Future<Void> delete(String name);
}
