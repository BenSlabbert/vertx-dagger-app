package com.example.catalog.service;

import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.DeleteOneResponseDto;
import com.example.catalog.web.route.dto.FindAllRequestDto;
import com.example.catalog.web.route.dto.FindAllResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemResponseDto;
import io.vertx.core.Future;

public interface ItemService {

  Future<FindAllResponseDto> findAll(FindAllRequestDto dto);

  Future<CreateItemResponseDto> create(CreateItemRequestDto dto);

  Future<FindOneResponseDto> findById(long id);

  Future<UpdateItemResponseDto> update(long id, UpdateItemRequestDto item);

  Future<DeleteOneResponseDto> delete(long id);
}
