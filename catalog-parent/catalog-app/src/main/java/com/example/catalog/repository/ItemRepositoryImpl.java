package com.example.catalog.repository;

import static com.example.commons.redis.RedisConstants.DOCUMENT_ROOT;
import static com.example.commons.redis.RedisConstants.SET_IF_DOES_NOT_EXIST;
import static com.example.commons.redis.RedisConstants.SET_IF_EXIST;
import static com.example.commons.redis.RedisConstants.STREAM_GENERATE_ID;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

import com.example.catalog.entity.Item;
import com.example.catalog.service.ItemService;
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

@Log
@Singleton
class ItemRepositoryImpl implements ItemRepository, AutoCloseable {

  private static final String CATALOG_STREAM = "catalog-stream";
  private static final String ITEM_SUGGESTION_DICTIONARY = "item-suggestions";
  private static final String ITEM_INDEX = "itemIdx";
  private static final String STREAM_FIELD = "item";
  private static final String ITEM_PREFIX = "item:";
  private static final String ITEM_SEQUENCE = "item-sequence";

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
                "$.sequence",
                "AS",
                "sequence",
                "NUMERIC",
                "SORTABLE",
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

    redisAPI
        .ftConfig(List.of("SET", "MAXSEARCHRESULTS", "1000"))
        .onFailure(err -> log.log(SEVERE, "failed to set MAXSEARCHRESULTS", err))
        .onSuccess(resp -> log.info("set MAXSEARCHRESULTS to 1000"));

    if (Boolean.parseBoolean(System.getenv("SEED"))) {
      this.redisAPI
          .eval(List.of("return #redis.pcall('keys', 'item:*')", "0"))
          .onFailure(err -> log.log(SEVERE, "failed to run eval", err))
          .onSuccess(
              resp -> {
                if (resp.type() != ResponseType.NUMBER) {
                  throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                }

                final long seedNumber = 25_000L;
                if (resp.toLong() < seedNumber) {
                  seed(seedNumber, 0L);
                }
              });
    }
  }

  @Override
  public Future<Item> create(String name, long priceInCents) {
    // todo should be multi
    return redisAPI
        .incr(ITEM_SEQUENCE)
        .compose(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              long sequence = resp.toLong();
              return createIfNotExists(sequence, name, priceInCents);
            })
        .onFailure(err -> log.log(SEVERE, "failed to create item", err))
        .onSuccess(
            createdItem -> {
              // update suggestions
              redisAPI
                  .ftSugadd(List.of(ITEM_SUGGESTION_DICTIONARY, name, Integer.toString(1)))
                  .onSuccess(resp -> log.info("added " + name + " to suggestion dictionary"));

              // emit on stream
              redisAPI
                  .xadd(
                      List.of(
                          CATALOG_STREAM,
                          STREAM_GENERATE_ID,
                          STREAM_FIELD,
                          createdItem.toJson().encode()))
                  .onSuccess(
                      resp -> {
                        if (resp.type() != ResponseType.BULK) {
                          throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                        }

                        log.log(
                            INFO,
                            "written new item to stream with id: {0}",
                            new Object[] {resp.toString()});
                      });
            });
  }

  @Override
  public Future<List<String>> suggest(String name) {
    return redisAPI
        .ftSugget(List.of(ITEM_SUGGESTION_DICTIONARY, name, "FUZZY", "MAX", Integer.toString(5)))
        .map(
            resp -> {
              if (null == resp) {
                return List.of();
              }

              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              return resp.stream().map(Response::toString).toList();
            });
  }

  @Override
  public Future<Page<Item>> findAll(long lastId, int size, ItemService.Direction direction) {
    return search(null, 0, 0, direction, lastId, size);
  }

  @Override
  public Future<Page<Item>> search(
      String name,
      int priceFrom,
      int priceTo,
      ItemService.Direction direction,
      long lastId,
      int size) {

    boolean forward = ItemService.Direction.FORWARD == direction;

    return redisAPI
        .multi()
        .compose(
            resp -> {
              if (!RedisConstants.OK.equals(resp.toString())) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              String query = getQuery(name, priceFrom, priceTo);
              query = addQueryParams(lastId, direction, query);

              return redisAPI.ftSearch(
                  List.of(
                      ITEM_INDEX,
                      query,
                      "SORTBY",
                      "sequence",
                      forward ? "ASC" : "DESC",
                      "RETURN",
                      "4",
                      "$.id",
                      "$.sequence",
                      "$.name",
                      "$.priceInCents",
                      "LIMIT",
                      "0",
                      Integer.toString((size + 1))));
            })
        .compose(resp -> enqueue(resp, redisAPI.ftInfo(List.of(ITEM_INDEX))))
        .compose(resp -> enqueue(resp, redisAPI.exec()))
        .map(
            resp -> {
              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              Iterator<Response> mainRespItr = resp.iterator();
              List<Item> items = parseSearchResponse(mainRespItr.next());
              Long numberOfDocs = parseInfoResponse(mainRespItr.next());

              boolean more = items.size() > size;
              if (more) {
                // subList before we sort as the results are backwards
                items = items.subList(0, size);
              }

              if (!forward) {
                // resort
                items.sort(Comparator.comparingLong(Item::sequence));
              }

              return new Page<>(more, numberOfDocs, items);
            });
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
    return findById(id)
        .compose(
            maybeItem -> {
              if (maybeItem.isEmpty()) {
                throw new HttpException(HttpResponseStatus.NOT_FOUND.code());
              }

              Item item = maybeItem.get();
              return update(new Item(id, item.sequence(), name, priceInCents));
            });
  }

  @Override
  public Future<Boolean> delete(UUID id) {
    return findById(id)
        .compose(
            maybeItem -> {
              if (maybeItem.isEmpty()) {
                // cannot find item to delete
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return delete(maybeItem.get());
            });
  }

  @Override
  public void close() {
    redisAPI.close();
  }

  private Future<Item> createIfNotExists(long sequence, String name, long priceInCents) {
    UUID id = UUID.randomUUID();
    Item item = new Item(id, sequence, name, priceInCents);
    String encoded = item.toJson().encode();

    return redisAPI
        .jsonSet(List.of(prefixId(id), DOCUMENT_ROOT, encoded, SET_IF_DOES_NOT_EXIST))
        .map(
            resp -> {
              if (null == resp) {
                // value could not be set
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              return item;
            });
  }

  private Future<Response> enqueue(Response resp, Future<Response> next) {
    if (!RedisConstants.QUEUED.equals(resp.toString())) {
      throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }

    return next;
  }

  private Future<Boolean> update(Item itemUpdate) {
    final String encodedItemUpdate = itemUpdate.toJson().encode();

    return redisAPI
        .watch(List.of(prefixId(itemUpdate.id())))
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
                  List.of(
                      prefixId(itemUpdate.id()), DOCUMENT_ROOT, encodedItemUpdate, SET_IF_EXIST));
            })
        .compose(
            resp ->
                enqueue(
                    resp,
                    redisAPI.xadd(
                        List.of(
                            CATALOG_STREAM, STREAM_GENERATE_ID, STREAM_FIELD, encodedItemUpdate))))
        .compose(resp -> enqueue(resp, redisAPI.exec()))
        .map(
            resp -> {
              if (null == resp) {
                // watch key was changed
                throw new HttpException(HttpResponseStatus.CONFLICT.code());
              }

              if (resp.type() != ResponseType.MULTI) {
                throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
              }

              // we only expect one response
              Response next = resp.iterator().next();

              return RedisConstants.OK.equals(next.toString());
            });
  }

  private Long parseInfoResponse(Response resp) {
    if (resp.type() != ResponseType.MULTI) {
      throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }

    Response numDocs = resp.get("num_docs");
    if (numDocs.type() != ResponseType.NUMBER) {
      throw new HttpException(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
    }

    return numDocs.toLong();
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
      var sequence = valuesItr.next();
      valuesItr.next();
      var itemName = valuesItr.next();
      valuesItr.next();
      var priceInCents = valuesItr.next();

      // strip prefix
      UUID uuid = UUID.fromString(id.toString());
      Item item = new Item(uuid, sequence.toLong(), itemName.toString(), priceInCents.toLong());
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

    if (searchByName && searchByPriceRange) {
      return "@name:(*"
          + name
          + "*)"
          + " "
          + String.format("@priceInCents:[%d %d]", priceFrom, priceTo);
    }

    if (searchByName) {
      return "@name:(*" + name + "*)";
    }

    if (searchByPriceRange) {
      return String.format("@priceInCents:[%d %d]", priceFrom, priceTo);
    }

    return null;
  }

  private String addQueryParams(long lastId, ItemService.Direction direction, String query) {
    boolean forward = ItemService.Direction.FORWARD == direction;

    String sequencePart =
        forward
            ? String.format("@sequence:[%d +inf]", lastId)
            : String.format("@sequence:[-inf %d]", lastId);

    if (null == query) {
      return "`" + sequencePart + "`";
    }

    return "`" + sequencePart + " " + query + " " + "`";
  }

  private Future<Boolean> delete(Item item) {
    return redisAPI
        .jsonDel(List.of(prefixId(item.id()), DOCUMENT_ROOT))
        .map(
            resp -> {
              if (resp.type() != ResponseType.NUMBER) {
                return Boolean.FALSE;
              }

              boolean deleted = resp.toLong() == 1L;

              if (deleted) {
                redisAPI
                    .ftSugdel(List.of(ITEM_SUGGESTION_DICTIONARY, item.name()))
                    .onFailure(
                        err ->
                            log.log(
                                SEVERE,
                                "failed to delete {0} from suggestion dictionary",
                                new Object[] {item.name()}))
                    .onSuccess(
                        r ->
                            log.log(
                                INFO,
                                "deleted {0} from suggestion dictionary",
                                new Object[] {item.name()}));
              }

              return deleted;
            });
  }

  private void seed(final long max, final long count) {
    if (count > max) {
      return;
    }

    create(RandomStringUtils.randomAlphabetic(10), RandomUtils.nextInt(1_00, 10_000))
        .onFailure(err -> log.log(SEVERE, "failed to save", err))
        .onSuccess(
            item -> {
              log.info("saved item: " + item.id());
              seed(max, count + 1L);
            });
  }

  private String prefixId(UUID id) {
    return ITEM_PREFIX + id;
  }
}
