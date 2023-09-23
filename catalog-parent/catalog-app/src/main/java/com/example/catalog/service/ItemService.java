/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import io.vertx.core.Future;
import java.util.Optional;

public interface ItemService {

  Future<PaginatedResponseDto> findAll(long lastId, int size);

  Future<SuggestResponseDto> suggest(String name);

  Future<CreateItemResponseDto> create(CreateItemRequestDto dto);

  Future<Optional<FindOneResponseDto>> findById(long id);

  Future<Void> update(long id, UpdateItemRequestDto item);

  Future<Void> delete(long id);
}
