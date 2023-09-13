package com.example.reactivetest.service;

import com.example.reactivetest.dao.sql.projection.PersonProjectionFactory.InsertReturningProjection.PersonProjection;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EventService {

  private static final String KAFKA_OUTBOX_SEND = "kafka.outbox.send";
  private static final String PERSON_CREATED = "person.created";

  private final Vertx vertx;
  private final PersonProjectionMessageCodec personProjectionMessageCodec =
      new PersonProjectionMessageCodec();

  @Inject
  EventService(Vertx vertx) {
    this.vertx = vertx;
    vertx.eventBus().registerCodec(personProjectionMessageCodec);
  }

  void publishPersonCreatedEvent(PersonProjection projection) {
    vertx
        .eventBus()
        .publish(
            PERSON_CREATED,
            projection,
            new DeliveryOptions().setCodecName(personProjectionMessageCodec.name()));
  }

  public MessageConsumer<PersonProjection> consumePersonCreatedEvent(
      Handler<PersonProjection> consumer) {
    return vertx.eventBus().consumer(PERSON_CREATED, m -> consumer.handle(m.body()));
  }

  void publishKafkaOutboxEvent(long id) {
    vertx.eventBus().publish(KAFKA_OUTBOX_SEND, id);
  }

  public MessageConsumer<Long> consumeKafkaOutboxEvent(Handler<Long> consumer) {
    return vertx.eventBus().consumer(KAFKA_OUTBOX_SEND, m -> consumer.handle(m.body()));
  }
}

// from:
// https://github.com/vert-x3/vertx-examples/blob/4.x/core-examples/src/main/java/io/vertx/example/core/eventbus/messagecodec/util/CustomMessageCodec.java
class PersonProjectionMessageCodec implements MessageCodec<PersonProjection, PersonProjection> {

  /** since we do not send over the wire we do not seen to serialize this */
  @Override
  public void encodeToWire(Buffer buffer, PersonProjection projection) {
    throw new UnsupportedOperationException("not implemented");
  }

  /** since we do not send over the wire we do not seen to serialize this */
  @Override
  public PersonProjection decodeFromWire(int position, Buffer buffer) {
    throw new UnsupportedOperationException("not implemented");
  }

  @Override
  public PersonProjection transform(PersonProjection projection) {
    // If a message is sent *locally* across the event bus.
    // This example sends message just as is
    return projection;
  }

  @Override
  public String name() {
    // Each codec must have a unique name.
    // This is used to identify a codec when sending a message and for unregistering codecs.
    return this.getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    // user codec
    return -1;
  }
}
