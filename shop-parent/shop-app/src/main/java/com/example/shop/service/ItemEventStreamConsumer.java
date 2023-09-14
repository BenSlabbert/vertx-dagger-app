/* Licensed under Apache-2.0 2023. */
package com.example.shop.service;

import static java.util.logging.Level.SEVERE;

import com.example.commons.config.Config;
import com.example.commons.redis.RedisConstants;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.ResponseType;
import io.vertx.redis.client.impl.types.NumberType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.java.Log;

@Log
@Singleton
public class ItemEventStreamConsumer implements AutoCloseable {

  private static final String SHOP_GROUP = "shop-group";
  private static final String CATALOG_STREAM = "catalog-stream";

  private final RedisAPI redisAPI;
  private final Vertx vertx;
  private Long timerId = 0L;

  @Inject
  ItemEventStreamConsumer(Vertx vertx, Config.RedisConfig redisConfig) {
    this.vertx = vertx;
    Redis client = Redis.createClient(vertx, redisConfig.uri());
    this.redisAPI = RedisAPI.api(client);

    this.redisAPI
        .ping(List.of(""))
        .onFailure(err -> log.log(SEVERE, "failed to ping redis", err))
        .onSuccess(resp -> log.info("pinged redis"))
        .compose(ignored -> redisAPI.xgroup(List.of("CREATE", CATALOG_STREAM, SHOP_GROUP, "0")))
        .onComplete(
            handler -> {
              if (handler.failed()
                  && (!"BUSYGROUP Consumer Group name already exists"
                      .equals(handler.cause().toString()))) {
                log.log(SEVERE, "failed to create consumer group", handler.cause());
                return;
              }

              timerId = vertx.setPeriodic(5000, id -> poll());
            });
  }

  private void poll() {
    log.info("polling");
    redisAPI
        .xpending(List.of(CATALOG_STREAM, SHOP_GROUP))
        .map(
            resp -> {
              if (resp.type() != ResponseType.MULTI) {
                throw new IllegalArgumentException("must be multi");
              }

              Iterator<Response> itr = resp.iterator();
              long numberOfPending = itr.next().toLong();

              return numberOfPending > 0L;
            })
        .onSuccess(
            pendingMessage -> {
              Future<List<Message>> messages;
              if (Boolean.TRUE.equals(pendingMessage)) {
                log.info("getPendingMessages");
                messages = getPendingMessages();
              } else {
                log.info("getNewMessages");
                messages = getNewMessages();
              }

              // fix this nested future
              messages
                  .flatMap(
                      msg -> {
                        if (msg.isEmpty()) {
                          log.info("no messages to process");
                          return Future.succeededFuture(NumberType.create(0L));
                        }

                        // handle messages
                        handleMessage(msg);

                        // ack messages
                        String[] array = msg.stream().map(Message::id).toArray(String[]::new);
                        String[] strings = {CATALOG_STREAM, SHOP_GROUP};
                        String[] combined = new String[array.length + strings.length];
                        System.arraycopy(strings, 0, combined, 0, strings.length);
                        System.arraycopy(array, 0, combined, strings.length, array.length);

                        return redisAPI.xack(List.of(combined));
                      })
                  .onSuccess(
                      resp -> {
                        if (null == resp) {
                          // nothing to do
                          return;
                        }

                        if (resp.type() != ResponseType.NUMBER) {
                          throw new IllegalArgumentException("must be a number");
                        }

                        Long aLong = resp.toLong();
                        log.info("acked: " + aLong);
                      });
            });
  }

  private void handleMessage(Iterable<Message> messages) {
    for (Message message : messages) {
      log.info("processing messageId: " + message.id());
      log.info("processing message body: " + message.body());
    }
  }

  private Future<List<Message>> getPendingMessages() {
    return redisAPI
        .xreadgroup(
            List.of(
                "GROUP",
                SHOP_GROUP,
                "consumer-name",
                // fetch 5 messages at a time
                "COUNT",
                "5",
                // block for 10 millis
                "BLOCK",
                "10",
                "STREAMS",
                CATALOG_STREAM,
                "0"))
        .map(this::getMessagesFromResponse);
  }

  private Future<List<Message>> getNewMessages() {
    return redisAPI
        .xreadgroup(
            List.of(
                "GROUP",
                SHOP_GROUP,
                "consumer-name",
                // fetch 5 messages at a time
                "COUNT",
                "5",
                // block for 10 millis
                "BLOCK",
                "10",
                "STREAMS",
                CATALOG_STREAM,
                RedisConstants.CONSUME_NEW_MESSAGES))
        .map(this::getMessagesFromResponse);
  }

  private List<Message> getMessagesFromResponse(Response resp) {
    if (null == resp) {
      return List.of();
    }

    if (resp.type() != ResponseType.MULTI) {
      throw new IllegalArgumentException("must be a multi");
    }

    List<Message> messages = new ArrayList<>(resp.getKeys().size());

    for (String key : resp.getKeys()) {
      Response responseForKey = resp.get(key);

      if (responseForKey.type() != ResponseType.MULTI) {
        throw new IllegalArgumentException("must be a multi");
      }

      for (Response response : responseForKey) {
        Iterator<Response> itr = response.iterator();
        Response messageId = itr.next();
        log.info("messageId: " + messageId);
        Response messagePayload = itr.next();
        Iterator<Response> itr2 = messagePayload.iterator();
        Response messageField = itr2.next();
        log.info("messageField: " + messageField);
        Response messageValue = itr2.next();
        log.info("messageValue: " + messageValue);
        messages.add(new Message(messageId.toString(), messageValue.toString()));
      }
    }

    return messages;
  }

  @Override
  public void close() {
    log.warning("shutting down consumer");
    log.info("cancelling timer: " + timerId);

    if (!vertx.cancelTimer(timerId)) {
      log.severe("failed to cancel timer: " + timerId);
    }

    redisAPI.close();
  }

  private record Message(String id, String body) {}
}
