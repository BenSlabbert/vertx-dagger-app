package com.example.catalog.repository;

import static com.example.commons.redis.RedisConstants.DOCUMENT_ROOT;
import static com.example.commons.redis.RedisConstants.SET_IF_DOES_NOT_EXIST;
import static com.example.commons.redis.RedisConstants.SET_IF_EXIST;
import static com.example.commons.redis.RedisConstants.STREAM_GENERATE_ID;
import static java.util.logging.Level.SEVERE;

import com.example.catalog.entity.Item;
import com.example.commons.config.Config;
import com.example.commons.redis.RedisConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.HttpException;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.ResponseType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
class ItemRepositoryImpl implements ItemRepository, AutoCloseable {

  private static final String CATALOG_STREAM = "catalog-stream";
  private static final String ITEM_INDEX = "itemIdx";
  private static final String STREAM_FIELD = "item";
  private static final String ITEM_PREFIX = "item:";

  private final RedisAPI redisAPI;

  @Inject
  ItemRepositoryImpl(Vertx vertx, Config.RedisConfig redisConfig) {
    Redis client = Redis.createClient(vertx, redisConfig.uri());
    this.redisAPI = RedisAPI.api(client);

    this.redisAPI
        .ping(List.of(""))
        .onFailure(err -> log.log(SEVERE, "failed to ping redis", err))
        .onSuccess(resp -> log.info("pinged redis"));

    this.redisAPI
        .ftCreate(
            List.of(
                ITEM_INDEX,
                "ON",
                "JSON",
                "PREFIX",
                "1",
                ITEM_PREFIX,
                "SCHEMA",
                "$.name",
                "AS",
                "name",
                "TEXT",
                "WITHSUFFIXTRIE",
                "SORTABLE",
                "UNF",
                "$.priceInCents",
                "AS",
                "priceInCents",
                "NUMERIC"))
        .onFailure(
            err -> {
              if ("Index already exists".equals(err.toString())) {
                log.info("index already exists, not an error");
                return;
              }
              log.log(SEVERE, "failed to create index", err);
            })
        .onSuccess(resp -> log.info("created index"));

    if (Boolean.parseBoolean(System.getenv("SEED"))) {
      this.redisAPI
          .eval(List.of("return #redis.pcall('keys', 'item:*')", "0"))
          .onFailure(err -> log.log(SEVERE, "failed to run eval", err))
          .onSuccess(
              resp -> {
                if (resp.type() != ResponseType.NUMBER) {
                  throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                }

                if (resp.toLong() < 25) {
                  for (int i = 0; i < 25; i++) {
                    create("seed_" + i, i);
                  }
                }
              });
    }
  }

  @Override
  public Future<Item> create(String name, long priceInCents) {
    UUID id = UUID.randomUUID();
    Item item = new Item(id, name, priceInCents);
    String encoded = item.toJson().encode();

    // todo run in a multi block
    //  we need all of these to be executed on the server
    return redisAPI
        .jsonSet(List.of(prefixId(id), DOCUMENT_ROOT, encoded, SET_IF_DOES_NOT_EXIST))
        .map(
            resp -> {
              if (null == resp) {
                // value could not be set
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return item;
            })
        .compose(
            ignore ->
                redisAPI.xadd(List.of(CATALOG_STREAM, STREAM_GENERATE_ID, STREAM_FIELD, encoded)))
        .map(
            resp -> {
              if (resp.type() != ResponseType.BULK) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              log.log(
                  Level.INFO,
                  "written new item to stream with id: {0}",
                  new Object[] {resp.toString()});

              return item;
            });
  }

  @Override
  public Future<Page<Item>> findAll(int page, int size) {
    int from = page * size;
    int to = from + size;

    return redisAPI
        .multi()
        .compose(
            resp -> {
              if (!RedisConstants.OK.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }
              return redisAPI.ftSearch(
                  List.of(
                      ITEM_INDEX,
                      "*",
                      "SORTBY",
                      "name",
                      "RETURN",
                      "3",
                      "$.id",
                      "$.name",
                      "$.priceInCents",
                      "LIMIT",
                      Integer.toString(from),
                      Integer.toString((to - from))));
            })
        .compose(
            resp -> {
              if (!RedisConstants.QUEUED.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }
              return redisAPI.ftInfo(List.of(ITEM_INDEX));
            })
        .compose(
            resp -> {
              if (!RedisConstants.QUEUED.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.exec();
            })
        .map(
            resp -> {
              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              Iterator<Response> mainRespItr = resp.iterator();
              List<Item> arr = parseSearchResponse(mainRespItr.next());
              Long numberOfDocs = parseInfoResponse(mainRespItr.next());

              return new Page<>(page, arr.size(), numberOfDocs, arr);
            });
  }

  private static Long parseInfoResponse(Response resp) {
    if (resp.type() != ResponseType.MULTI) {
      throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }

    Response numDocs = resp.get("num_docs");
    if (numDocs.type() != ResponseType.NUMBER) {
      throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }

    return numDocs.toLong();
  }

  @Override
  public Future<List<Item>> search(String name, int priceFrom, int priceTo, int page, int size) {
    int from = page * size;
    int to = from + size;

    return redisAPI
        .ftSearch(
            List.of(
                ITEM_INDEX,
                getQuery(name, priceFrom, priceTo),
                "SORTBY",
                "name",
                "RETURN",
                "3",
                "$.id",
                "$.name",
                "$.priceInCents",
                "LIMIT",
                Integer.toString(from),
                Integer.toString((to - from))))
        .map(this::parseSearchResponse);
  }

  private List<Item> parseSearchResponse(Response resp) {
    if (resp.type() != ResponseType.MULTI) {
      throw new IllegalArgumentException("should be multi");
    }

    Iterator<Response> respItr = resp.iterator();
    Response numberOfItems = respItr.next();

    if (numberOfItems.type() != ResponseType.NUMBER) {
      throw new IllegalArgumentException("should be number");
    }

    if (numberOfItems.toLong() == 0L) {
      return List.of();
    }

    List<Item> items = new ArrayList<>();

    while (respItr.hasNext()) {
      Response key = respItr.next();
      if (key.type() != ResponseType.BULK) {
        throw new IllegalArgumentException("should be bulk");
      }

      Response values = respItr.next();
      if (values.type() != ResponseType.MULTI) {
        throw new IllegalArgumentException("should be multi");
      }

      Iterator<Response> valuesItr = values.iterator();
      // every other value
      valuesItr.next();
      var id = valuesItr.next();
      valuesItr.next();
      var itemName = valuesItr.next();
      valuesItr.next();
      var priceInCents = valuesItr.next();

      // strip prefix
      UUID uuid = UUID.fromString(id.toString());
      Item item = new Item(uuid, itemName.toString(), priceInCents.toLong());
      items.add(item);
    }

    return items;
  }

  private String getQuery(String name, int priceFrom, int priceTo) {
    boolean searchByName = true;
    boolean searchByPriceRange = true;

    if (null == name) {
      // no argument given, exclude from query
      searchByName = false;
    }

    if (priceFrom == 0 && priceTo == 0) {
      // no arguments given, exclude from query
      searchByPriceRange = false;
    }

    // be careful of the ` back ticks and white space
    if (searchByName && searchByPriceRange) {
      return "`@name:(*"
          + name
          + "*)"
          + " "
          + String.format("@priceInCents:[%d %d]`", priceFrom, priceTo);
    }

    if (searchByName) {
      return "`@name:(*" + name + "*)`";
    }

    if (searchByPriceRange) {
      return String.format("`@priceInCents:[%d %d]`", priceFrom, priceTo);
    }

    // client should specify at least one of these
    throw new HttpException(HttpResponseStatus.BAD_REQUEST.code());
  }

  @Override
  public Future<Optional<Item>> findById(UUID id) {
    return redisAPI
        .jsonGet(List.of(prefixId(id), DOCUMENT_ROOT))
        .map(
            resp -> {
              if (resp == null || resp.type() != ResponseType.BULK) {
                return Optional.empty();
              }

              String string = resp.toString();
              string = string.substring(1, string.length() - 1);
              return Optional.of(new Item(new JsonObject(string)));
            });
  }

  @Override
  public Future<Boolean> update(UUID id, String name, long priceInCents) {
    final String encodedItemUpdate = new Item(id, name, priceInCents).toJson().encode();

    return redisAPI
        .watch(List.of(prefixId(id)))
        .compose(
            resp -> {
              if (!RedisConstants.OK.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.multi();
            })
        .compose(
            resp -> {
              if (!RedisConstants.OK.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.jsonSet(
                  List.of(prefixId(id), DOCUMENT_ROOT, encodedItemUpdate, SET_IF_EXIST));
            })
        .compose(
            resp -> {
              if (!RedisConstants.QUEUED.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.xadd(
                  List.of(CATALOG_STREAM, STREAM_GENERATE_ID, STREAM_FIELD, encodedItemUpdate));
            })
        .compose(
            resp -> {
              if (!RedisConstants.QUEUED.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return redisAPI.exec();
            })
        .map(
            resp -> {
              // todo: retry if transaction was aborted due to object being changed
              //  or send back an erro to client
              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              // we only expect one response
              Response next = resp.iterator().next();

              return RedisConstants.OK.equals(next.toString());
            });
  }

  @Override
  public Future<Boolean> delete(UUID id) {
    // todo run in a multi/watch block
    //  check if the given dto has the same version of the persisted one
    return redisAPI
        .jsonDel(List.of(prefixId(id), DOCUMENT_ROOT))
        .map(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                return Boolean.FALSE;
              }

              return resp.toLong() == 1L;
            });
  }

  private static String prefixId(UUID id) {
    return ITEM_PREFIX + id;
  }

  @Override
  public void close() {
    redisAPI.close();
  }
}
