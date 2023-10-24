/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.mapper.FindOneResponseDtoMapper;
import com.example.catalog.projection.item.ItemProjection;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.SuggestionService;
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
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class ItemService extends TransactionBoundary {

  private final ItemRepository itemRepository;
  private final SagaService sagaService;
  private final SuggestionService suggestionService;
  private final FindOneResponseDtoMapper findOneResponseDtoMapper;

  @Inject
  ItemService(
      PgPool pool,
      ItemRepository itemRepository,
      SagaService sagaService,
      SuggestionService cache,
      FindOneResponseDtoMapper findOneResponseDtoMapper) {
    super(pool);
    this.itemRepository = itemRepository;
    this.sagaService = sagaService;
    this.suggestionService = cache;
    this.findOneResponseDtoMapper = findOneResponseDtoMapper;
  }

  public Future<String> execute() {
    return sagaService
        .createPurchaseOrderSaga()
        .execute()
        .onFailure(err -> log.log(Level.SEVERE, "failed to execute saga", err))
        .onSuccess(sagaId -> log.info("%s: saga completed".formatted(sagaId)));
  }

  public Future<PaginatedResponseDto> findAll(long lastId, int size) {
    Future<List<ItemProjection>> page =
        doInTransaction(conn -> itemRepository.getPage(conn, lastId, size + 1));

    return page.map(projections -> projections.stream().map(findOneResponseDtoMapper::map).toList())
        .map(
            items -> {
              if (items.isEmpty()) {
                return new PaginatedResponseDto(false, 0, List.of());
              }

              var more = items.size() > size;

              if (more) {
                items = items.subList(0, items.size() - 1);
              }

              return new PaginatedResponseDto(more, items.size(), items);
            });
  }

  public Future<SuggestResponseDto> suggest(String name) {
    return suggestionService.suggest(name).map(SuggestResponseDto::new);
  }

  public Future<CreateItemResponseDto> create(CreateItemRequestDto dto) {
    Future<CreatedItemProjection> createdItem =
        doInTransaction(conn -> itemRepository.create(conn, dto.name(), dto.priceInCents()));

    return createdItem
        .onSuccess(item -> suggestionService.create(dto.name()))
        .map(
            item ->
                new CreateItemResponseDto(
                    item.id(), dto.name(), dto.priceInCents(), item.version()));
  }

  public Future<Optional<FindOneResponseDto>> findById(long id) {
    Future<Optional<ItemProjection>> itemProjection =
        doInTransaction(conn -> itemRepository.findById(conn, id));

    return itemProjection.map(maybeItem -> maybeItem.map(findOneResponseDtoMapper::map));
  }

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
                              .update(conn, id, dto.name(), dto.priceInCents(), dto.version())
                              .map(ignore -> maybeItem.get());
                        }));

    return itemProjection
        .onSuccess(oldItem -> suggestionService.update(oldItem.getName(), dto.name()))
        .map(ignore -> null);
  }

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
