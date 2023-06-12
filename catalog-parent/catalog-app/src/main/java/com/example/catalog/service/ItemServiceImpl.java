package com.example.catalog.service;

import com.example.catalog.repository.ItemRepository;
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
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemServiceImpl implements ItemService {

  private final ItemRepository itemRepository;

  @Inject
  ItemServiceImpl(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public Future<FindAllResponseDto> findAll(int from, int to) {
    return itemRepository
        .findAll(from, to)
        .map(
            items ->
                items.stream()
                    .map(
                        item -> new FindOneResponseDto(item.id(), item.name(), item.priceInCents()))
                    .toList())
        .map(FindAllResponseDto::new);
  }

  @Override
  public Future<FindAllResponseDto> searchByName(String name) {
    return itemRepository
        .searchByName(name)
        .map(
            items ->
                items.stream()
                    .map(
                        item -> new FindOneResponseDto(item.id(), item.name(), item.priceInCents()))
                    .toList())
        .map(FindAllResponseDto::new);
  }

  @Override
  public Future<CreateItemResponseDto> create(CreateItemRequestDto dto) {
    return itemRepository
        .create(dto.name(), dto.priceInCents())
        .map(item -> new CreateItemResponseDto(item.id(), item.name(), item.priceInCents()));
  }

  @Override
  public Future<Optional<FindOneResponseDto>> findById(UUID id) {
    return itemRepository
        .findById(id)
        .map(
            maybeItem ->
                maybeItem.map(
                    item -> new FindOneResponseDto(item.id(), item.name(), item.priceInCents())));
  }

  @Override
  public Future<Optional<UpdateItemResponseDto>> update(UUID id, UpdateItemRequestDto dto) {
    return itemRepository
        .update(id, dto.name(), dto.priceInCents())
        .map(
            success ->
                Boolean.TRUE.equals(success)
                    ? Optional.of(new UpdateItemResponseDto())
                    : Optional.empty());
  }

  @Override
  public Future<Optional<DeleteOneResponseDto>> delete(UUID id) {
    return itemRepository
        .delete(id)
        .map(
            success ->
                Boolean.TRUE.equals(success)
                    ? Optional.of(new DeleteOneResponseDto())
                    : Optional.empty());
  }
}
