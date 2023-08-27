package com.example.reactivetest.service;

import static com.example.reactivetest.config.KafkaTopics.TOPIC;

import com.example.reactivetest.dao.sql.projection.OutboxProjectionFactory;
import com.example.reactivetest.dao.sql.projection.PersonProjectionFactory.InsertReturningProjection.PersonProjection;
import com.example.reactivetest.proto.Version;
import com.example.reactivetest.proto.core.Header;
import com.example.reactivetest.proto.core.Headers;
import com.example.reactivetest.proto.v1.Person;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import lombok.SneakyThrows;

class KafkaMessageFactory {

  private static final String VERSION_HEADER = "X-Protocol-Version";

  private KafkaMessageFactory() {}

  static KafkaProducerRecord<String, Person> create(PersonProjection projection) {
    var person = Person.newBuilder().setId(projection.id()).setName(projection.name()).build();

    return KafkaProducerRecord.create(TOPIC, Long.toString(projection.id()), person, 0)
        .addHeader(VERSION_HEADER, Version.V1.toString());
  }

  @SneakyThrows
  static KafkaProducerRecord<String, Person> create(
      OutboxProjectionFactory.GetFromOutbox.GetFromOutboxProjection outboxProjection) {

    Headers headers = Headers.parseFrom(outboxProjection.headers());

    KafkaProducerRecord<String, Person> msg =
        KafkaProducerRecord.create(
            TOPIC, outboxProjection.key(), Person.parseFrom(outboxProjection.value()), 0);

    for (Header header : headers.getHeadersList()) {
      msg = msg.addHeader(header.getKey(), header.getValue());
    }

    return msg;
  }
}
