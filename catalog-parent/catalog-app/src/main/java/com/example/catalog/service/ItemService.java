/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.DeleteOneResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemResponseDto;
import io.vertx.core.Future;
import java.util.Optional;
import java.util.UUID;

public interface ItemService {

  enum Direction {
    FORWARD,
    BACKWARD
  }

  Future<PaginatedResponseDto> findAll(long lastId, int size, Direction direction);

  Future<PaginatedResponseDto> search(
      String name,
      int priceFrom,
      int priceTo,
      ItemService.Direction direction,
      long lastId,
      int size);

  Future<SuggestResponseDto> suggest(String name);

  Future<CreateItemResponseDto> create(CreateItemRequestDto dto);

  Future<Optional<FindOneResponseDto>> findById(UUID id);

  Future<Optional<UpdateItemResponseDto>> update(UUID id, UpdateItemRequestDto item);

  Future<Optional<DeleteOneResponseDto>> delete(UUID id);
}
