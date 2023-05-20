package com.example.catalog.service;

import com.example.catalog.entity.Item;
import com.example.catalog.repository.ItemRepository;
import com.example.catalog.web.route.dto.CreateItemRequestDto;
import com.example.catalog.web.route.dto.CreateItemResponseDto;
import com.example.catalog.web.route.dto.DeleteOneResponseDto;
import com.example.catalog.web.route.dto.FindAllRequestDto;
import com.example.catalog.web.route.dto.FindAllResponseDto;
import com.example.catalog.web.route.dto.FindOneResponseDto;
import com.example.catalog.web.route.dto.UpdateItemRequestDto;
import com.example.catalog.web.route.dto.UpdateItemResponseDto;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemServiceImpl implements ItemService {

  private final Pool dbPool;
  private final Emitter emitter;
  private final ItemRepository itemRepository;

  @Inject
  ItemServiceImpl(Pool pool, Emitter emitter, ItemRepository itemRepository) {
    this.dbPool = pool;
    this.itemRepository = itemRepository;
    this.emitter = emitter;
  }

  @Override
  public Future<FindAllResponseDto> findAll(FindAllRequestDto dto) {
    return doInTransaction(itemRepository::findAll)
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
    return doInTransaction(conn -> itemRepository.create(conn, dto.name(), dto.priceInCents()))
        .onSuccess(emitter::emit)
        .map(item -> new CreateItemResponseDto(item.id(), item.name(), item.priceInCents()));
  }

  @Override
  public Future<Optional<FindOneResponseDto>> findById(UUID id) {
    return doInTransaction(conn -> itemRepository.findById(conn, id))
        .map(
            maybeItem ->
                maybeItem.map(
                    item -> new FindOneResponseDto(item.id(), item.name(), item.priceInCents())));
  }

  @Override
  public Future<Optional<UpdateItemResponseDto>> update(UUID id, UpdateItemRequestDto dto) {
    return doInTransaction(conn -> itemRepository.update(conn, id, dto.name(), dto.priceInCents()))
        .onSuccess(
            success -> {
              if (Boolean.TRUE.equals(success)) {
                emitter.emit(new Item(id, dto.name(), dto.priceInCents()));
              }
            })
        .map(
            success ->
                Boolean.TRUE.equals(success)
                    ? Optional.of(new UpdateItemResponseDto())
                    : Optional.empty());
  }

  @Override
  public Future<Optional<DeleteOneResponseDto>> delete(UUID id) {
    return doInTransaction(conn -> itemRepository.delete(conn, id))
        .map(
            success ->
                Boolean.TRUE.equals(success)
                    ? Optional.of(new DeleteOneResponseDto())
                    : Optional.empty());
  }

  private <T> Future<T> doInTransaction(Function<SqlConnection, Future<T>> futureFunction) {
    return dbPool
        .getConnection()
        .compose(
            conn ->
                conn.begin()
                    .compose(tx -> futureFunction.apply(conn).eventually(v -> tx.commit()))
                    .eventually(v -> conn.close())
                    .onSuccess(t -> log.log(Level.INFO, "Transaction succeeded"))
                    .onFailure(err -> log.log(Level.SEVERE, "Transaction failed", err)));
  }
}
