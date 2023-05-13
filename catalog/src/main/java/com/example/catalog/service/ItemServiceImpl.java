package com.example.catalog.service;

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
import java.util.function.Function;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemServiceImpl implements ItemService {

  private final Pool dbPool;
  private final ItemRepository itemRepository;

  @Inject
  ItemServiceImpl(Pool pool, ItemRepository itemRepository) {
    this.dbPool = pool;
    this.itemRepository = itemRepository;
  }

  @Override
  public Future<FindAllResponseDto> findAll(FindAllRequestDto dto) {
    return doInTransaction(itemRepository::findAll)
        .map(
            items ->
                items.stream()
                    .map(
                        item ->
                            new FindOneResponseDto(
                                item.id(), item.uuid(), item.name(), item.priceInCents()))
                    .toList())
        .map(FindAllResponseDto::new);
  }

  @Override
  public Future<CreateItemResponseDto> create(CreateItemRequestDto dto) {
    return doInTransaction(conn -> itemRepository.create(conn, dto.name(), dto.priceInCents()))
        .map(
            item ->
                new CreateItemResponseDto(
                    item.id(), item.uuid(), item.name(), item.priceInCents()));
  }

  @Override
  public Future<FindOneResponseDto> findById(long id) {
    return doInTransaction(conn -> itemRepository.findById(conn, id))
        .map(
            item ->
                new FindOneResponseDto(item.id(), item.uuid(), item.name(), item.priceInCents()));
  }

  @Override
  public Future<UpdateItemResponseDto> update(long id, UpdateItemRequestDto dto) {
    return doInTransaction(conn -> itemRepository.update(conn, id, dto.name(), dto.priceInCents()))
        .map(v -> new UpdateItemResponseDto());
  }

  @Override
  public Future<DeleteOneResponseDto> delete(long id) {
    return doInTransaction(conn -> itemRepository.delete(conn, id))
        .map(v -> new DeleteOneResponseDto());
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
