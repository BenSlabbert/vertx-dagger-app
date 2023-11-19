/* Licensed under Apache-2.0 2023. */
package com.example.payment.scope.repo;

import java.util.stream.Stream;

public interface Repo {

  Stream<Projection> fetchAll();

  Projection fetch(long id);

  Long save(String name);

  record Projection(long id, String name, long version) {}
}
