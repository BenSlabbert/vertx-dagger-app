/* Licensed under Apache-2.0 2023. */
package com.example.commons.eventbus;

import static org.assertj.core.api.Assertions.assertThat;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
class EventBusTest {

  @AfterEach
  void after() {
    System.err.println("ending test");
  }

  @Test
  void testReplyFailures_timeout(Vertx vertx, VertxTestContext testContext) {
    EventBus eventBus = vertx.eventBus();

    eventBus.consumer(
        "test",
        (Message<String> msg) -> {
          // do not reply
        });

    eventBus.request(
        "test",
        "request",
        new DeliveryOptions().setSendTimeout(100L),
        (AsyncResult<Message<String>> ar) ->
            testContext.verify(
                () -> {
                  assertThat(ar.failed()).isTrue();
                  Throwable err = ar.cause();
                  assertThat(err).isInstanceOf(ReplyException.class);
                  ReplyException ex = (ReplyException) err;
                  assertThat(ex.failureType()).isEqualTo(ReplyFailure.TIMEOUT);
                  assertThat(ex.failureCode()).isEqualTo(-1);
                  testContext.completeNow();
                }));
  }

  @Test
  void testReplyFailures_noHandlers(Vertx vertx, VertxTestContext testContext) {
    EventBus eventBus = vertx.eventBus();

    eventBus.request(
        "test",
        "request",
        (AsyncResult<Message<String>> ar) ->
            testContext.verify(
                () -> {
                  Throwable err = ar.cause();
                  assertThat(err).isInstanceOf(ReplyException.class);
                  ReplyException ex = (ReplyException) err;
                  assertThat(ex.failureType()).isEqualTo(ReplyFailure.NO_HANDLERS);
                  assertThat(ex.failureCode()).isEqualTo(-1);
                  testContext.completeNow();
                }));
  }

  @Test
  void testReplyFailures_recipientFailure(Vertx vertx, VertxTestContext testContext) {
    EventBus eventBus = vertx.eventBus();

    eventBus.consumer("test", (Message<String> msg) -> msg.fail(100, "deliberate failure"));

    eventBus.request(
        "test",
        "request",
        (AsyncResult<Message<String>> ar) ->
            testContext.verify(
                () -> {
                  Throwable err = ar.cause();
                  assertThat(err).isInstanceOf(ReplyException.class);
                  ReplyException ex = (ReplyException) err;

                  assertThat(ex.failureType()).isEqualTo(ReplyFailure.RECIPIENT_FAILURE);
                  assertThat(ex.failureCode()).isEqualTo(100);
                  assertThat(ex.getMessage()).isEqualTo("deliberate failure");
                  testContext.completeNow();
                }));
  }

  @Test
  void sendToOne(Vertx vertx, VertxTestContext testContext) {
    Checkpoint messageReceivedByConsumer = testContext.checkpoint(3);
    EventBus eventBus = vertx.eventBus();

    MessageConsumer<String> consumer =
        eventBus.consumer(
            "test",
            msg -> {
              msg.reply("reply", new DeliveryOptions().addHeader("key", "value"));
              messageReceivedByConsumer.flag();
            });

    consumer.completionHandler(
        ar -> System.err.println("registration completed, success ? " + ar.succeeded()));

    // called when the ReadStream is closed
    consumer.endHandler(ignore -> System.err.println("stream ended"));

    eventBus
        .send("test", "send")
        .publish("test", "publish")
        .request(
            "test",
            "request",
            (AsyncResult<Message<String>> ar) ->
                testContext.verify(
                    () -> {
                      assertThat(ar.cause()).isNull();
                      assertThat(ar.failed()).isFalse();
                      assertThat(ar.succeeded()).isTrue();
                      Message<String> result = ar.result();
                      assertThat(result.body()).isEqualTo("reply");
                      assertThat(result.headers().contains("key", "value", false)).isTrue();
                      testContext.completeNow();
                    }));
  }
}
