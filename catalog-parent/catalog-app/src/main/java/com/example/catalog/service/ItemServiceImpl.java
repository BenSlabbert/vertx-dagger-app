/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.repository.SuggestionService;
import com.example.catalog.repository.sql.ItemRepository;
import com.example.catalog.repository.sql.projection.ItemProjectionFactory.InsertItemProjection.CreatedItemProjection;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.commons.transaction.TransactionBoundary;
import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.pgclient.PgPool;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemServiceImpl extends TransactionBoundary implements ItemService {

  private final ItemRepository itemRepository;
  private final SuggestionService suggestionService;

  @Inject
  ItemServiceImpl(PgPool pool, ItemRepository itemRepository, SuggestionService cache) {
    super(pool);
    this.itemRepository = itemRepository;
    this.suggestionService = cache;
  }

  @Override
  public Future<PaginatedResponseDto> findAll(long lastId, int size) {
    Future<List<ItemProjection>> page =
        doInTransaction(conn -> itemRepository.getPage(conn, lastId, size));

    return page.map(
        itemProjections -> {
          List<FindOneResponseDto> items =
              itemProjections.stream()
                  .map(
                      item ->
                          new FindOneResponseDto(
                              item.getId(), item.getName(), item.getPriceInCents()))
                  .toList();

          return new PaginatedResponseDto(true, items.size(), items);
        });
  }

  @Override
  public Future<SuggestResponseDto> suggest(String name) {
    return suggestionService.suggest(name).map(SuggestResponseDto::new);
  }

  @Override
  public Future<CreateItemResponseDto> create(CreateItemRequestDto dto) {
    Future<CreatedItemProjection> createdItem =
        doInTransaction(conn -> itemRepository.create(conn, dto.name(), dto.priceInCents()));

    return createdItem
        .onSuccess(item -> suggestionService.create(dto.name()))
        .map(item -> new CreateItemResponseDto(item.id(), dto.name(), dto.priceInCents()));
  }

  @Override
  public Future<Optional<FindOneResponseDto>> findById(long id) {
    Future<Optional<ItemProjection>> itemProjection =
        doInTransaction(conn -> itemRepository.findById(conn, id));

    return itemProjection.map(
        maybeItem ->
            maybeItem.map(
                item ->
                    new FindOneResponseDto(item.getId(), item.getName(), item.getPriceInCents())));
  }

  @Override
  public Future<Void> update(long id, UpdateItemRequestDto dto) {
    Future<ItemProjection> itemProjection =
        doInTransaction(
            conn ->
                itemRepository
                    .findById(conn, id)
                    .compose(
                        maybeItem -> {
                          if (maybeItem.isEmpty()) {
                            throw new NoStackTraceException("no item for id: " + id);
                          }

                          return itemRepository
                              .update(conn, id, dto.name(), dto.priceInCents())
                              .map(ignore -> maybeItem.get());
                        }));

    return itemProjection
        .onSuccess(oldItem -> suggestionService.update(oldItem.getName(), dto.name()))
        .map(ignore -> null);
  }

  @Override
  public Future<Void> delete(long id) {
    Future<ItemProjection> itemProjection =
        doInTransaction(
            conn ->
                itemRepository
                    .findById(conn, id)
                    .compose(
                        maybeItem -> {
                          if (maybeItem.isEmpty()) {
                            throw new NoStackTraceException("no item for id: " + id);
                          }

                          return itemRepository.delete(conn, id).map(ignore -> maybeItem.get());
                        }));

    return itemProjection
        .onSuccess(item -> suggestionService.delete(item.getName()))
        .map(ignore -> null);
  }
}
