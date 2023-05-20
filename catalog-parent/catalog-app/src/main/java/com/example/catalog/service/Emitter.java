package com.example.catalog.service;

import com.example.catalog.entity.Item;
import io.vertx.core.Future;

public interface Emitter {

  Future<Void> emit(Item item);
}
