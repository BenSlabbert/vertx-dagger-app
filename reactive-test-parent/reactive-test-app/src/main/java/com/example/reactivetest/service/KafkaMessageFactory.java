/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import static com.example.reactivetest.config.KafkaTopics.TOPIC;

import com.example.reactivetest.proto.Version;
import com.example.reactivetest.proto.core.Header;
import com.example.reactivetest.proto.core.Headers;
import com.example.reactivetest.proto.v1.Person;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory;
import com.example.reactivetest.repository.sql.projection.PersonProjectionFactory.PersonProjection;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import java.util.UUID;
import lombok.SneakyThrows;

class KafkaMessageFactory {

  private static final String VERSION_HEADER = "X-Protocol-Version";
  private static final String RECORD_UUID_HEADER = "X-UUID";
  private static final int PARTITION = 0;

  private KafkaMessageFactory() {}

  static KafkaProducerRecord<String, Person> create(PersonProjection projection) {
    var person = Person.newBuilder().setId(projection.id()).setName(projection.name()).build();

    return KafkaProducerRecord.create(TOPIC, Long.toString(projection.id()), person, PARTITION)
        .addHeader(RECORD_UUID_HEADER, UUID.randomUUID().toString())
        .addHeader(VERSION_HEADER, Version.V1.toString());
  }

  @SneakyThrows
  static KafkaProducerRecord<String, Person> create(
      OutboxProjectionFactory.GetFromOutboxProjection outboxProjection) {

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
