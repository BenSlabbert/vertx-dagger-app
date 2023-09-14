/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.repository.ItemRepository;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.DeleteOneResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemResponseDto;
import io.vertx.core.Future;
import java.util.List;
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
  public Future<PaginatedResponseDto> findAll(long lastId, int size, Direction direction) {
    return itemRepository
        .findAll(lastId, size, direction)
        .map(
            pageOfItems -> {
              List<FindOneResponseDto> items =
                  pageOfItems.items().stream()
                      .map(
                          item ->
                              new FindOneResponseDto(
                                  item.id(), item.sequence(), item.name(), item.priceInCents()))
                      .toList();

              return new PaginatedResponseDto(pageOfItems.more(), pageOfItems.total(), items);
            });
  }

  @Override
  public Future<PaginatedResponseDto> search(
      String name,
      int priceFrom,
      int priceTo,
      ItemService.Direction direction,
      long lastId,
      int size) {

    return itemRepository
        .search(name, priceFrom, priceTo, direction, lastId, size)
        .map(
            pageOfItems -> {
              List<FindOneResponseDto> items =
                  pageOfItems.items().stream()
                      .map(
                          item ->
                              new FindOneResponseDto(
                                  item.id(), item.sequence(), item.name(), item.priceInCents()))
                      .toList();

              return new PaginatedResponseDto(pageOfItems.more(), pageOfItems.total(), items);
            });
  }

  @Override
  public Future<SuggestResponseDto> suggest(String name) {
    return itemRepository.suggest(name).map(SuggestResponseDto::new);
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
                    item ->
                        new FindOneResponseDto(
                            item.id(), item.sequence(), item.name(), item.priceInCents())));
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
