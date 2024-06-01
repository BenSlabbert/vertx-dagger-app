/* Licensed under Apache-2.0 2023. */
package com.example.catalog.service;

import com.example.catalog.mapper.FindOneResponseDtoMapper;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.repository.SuggestionService;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.PaginatedResponseDto;
import com.example.catalog.web.route.dto.SuggestResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import github.benslabbert.vertxdaggercommons.transaction.reactive.TransactionBoundary;
import io.vertx.core.Future;
import io.vertx.core.impl.NoStackTraceException;
import io.vertx.sqlclient.Pool;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ItemService extends TransactionBoundary {

  private static final Logger log = LoggerFactory.getLogger(ItemService.class);

  private final ItemRepository itemRepository;
  private final SagaService sagaService;
  private final SuggestionService suggestionService;
  private final FindOneResponseDtoMapper findOneResponseDtoMapper;

  @Inject
  ItemService(
      Pool pool,
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
        .onFailure(err -> log.error("failed to execute saga", err))
        .onSuccess(sagaId -> log.info("{}: saga completed", sagaId));
  }

  public Future<PaginatedResponseDto> nextPage(long fromId, int size) {
    return doInTransaction(conn -> itemRepository.nextPage(conn, fromId, size + 1))
        .map(projections -> projections.stream().map(findOneResponseDtoMapper::map).toList())
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

  public Future<PaginatedResponseDto> previousPage(long fromId, int size) {
    return doInTransaction(conn -> itemRepository.previousPage(conn, fromId, size + 1))
        .map(projections -> projections.stream().map(findOneResponseDtoMapper::map).toList())
        .map(
            items -> {
              if (items.isEmpty()) {
                return new PaginatedResponseDto(false, 0, List.of());
              }

              var more = items.size() > size;

              if (more) {
                items = items.subList(1, items.size());
              }

              return new PaginatedResponseDto(more, items.size(), items);
            });
  }

  public Future<SuggestResponseDto> suggest(String name) {
    return suggestionService.suggest(name).map(SuggestResponseDto::new);
  }

  public Future<CreateItemResponseDto> create(CreateItemRequestDto dto) {
    return doInTransaction(conn -> itemRepository.create(conn, dto.name(), dto.priceInCents()))
        .onSuccess(ignore -> suggestionService.create(dto.name()))
        .map(
            item ->
                new CreateItemResponseDto(
                    item.id(), dto.name(), dto.priceInCents(), item.version()));
  }

  public Future<Optional<FindOneResponseDto>> findById(long id) {
    return doInTransaction(conn -> itemRepository.findById(conn, id))
        .map(maybeItem -> maybeItem.map(findOneResponseDtoMapper::map));
  }

  public Future<Void> update(long id, UpdateItemRequestDto dto) {
    return doInTransaction(
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
                              .map(ignore1 -> maybeItem.get());
                        }))
        .onSuccess(oldItem -> suggestionService.update(oldItem.name(), dto.name()))
        .map(ignore -> null);
  }

  public Future<Void> delete(long id) {
    return doInTransaction(
            conn ->
                itemRepository
                    .findById(conn, id)
                    .compose(
                        maybeItem -> {
                          if (maybeItem.isEmpty()) {
                            throw new NoStackTraceException("no item for id: " + id);
                          }

                          return itemRepository.delete(conn, id).map(ignore1 -> maybeItem.get());
                        }))
        .onSuccess(item -> suggestionService.delete(item.name()))
        .map(ignore -> null);
  }
}
