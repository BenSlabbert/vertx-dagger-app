package com.example.catalog.service;

import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.DeleteOneResponseDto;
import com.example.catalog.web.route.dto.FindAllResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemResponseDto;
import io.vertx.core.Future;
import java.util.Optional;
import java.util.UUID;

public interface ItemService {

  Future<FindAllResponseDto> findAll(int from, int to);

  Future<FindAllResponseDto> searchByName(String name);

  Future<CreateItemResponseDto> create(CreateItemRequestDto dto);

  Future<Optional<FindOneResponseDto>> findById(UUID id);

  Future<Optional<UpdateItemResponseDto>> update(UUID id, UpdateItemRequestDto item);

  Future<Optional<DeleteOneResponseDto>> delete(UUID id);
}
