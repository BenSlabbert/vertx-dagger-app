package com.example.reactivetest.service;

import com.example.reactivetest.dao.sql.OutboxRepository;
import com.example.reactivetest.dao.sql.projection.OutboxProjectionFactory;
import com.example.reactivetest.proto.core.Header;
import com.example.reactivetest.proto.core.Headers;
import com.example.reactivetest.proto.v1.Person;
import io.vertx.core.Future;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class KafkaOutboxService {

  private final OutboxRepository outboxRepository;

  @Inject
  KafkaOutboxService(OutboxRepository outboxRepository) {
    this.outboxRepository = outboxRepository;
  }

  Future<OutboxProjectionFactory.InsertIntoOutbox.InsertOutboxProjection> insert(
      SqlClient conn, KafkaProducerRecord<String, Person> msg) {

    String key = msg.key();
    byte[] value = msg.value().toByteArray();
    List<Header> headerList =
        msg.headers().stream()
            .map(h -> Header.newBuilder().setKey(h.key()).setValue(h.value().toString()).build())
            .toList();

    byte[] headers = Headers.newBuilder().addAllHeaders(headerList).build().toByteArray();

    return outboxRepository.insert(conn, key, headers, value);
  }

  Future<OutboxProjectionFactory.DeleteFromOutbox.DeleteOutboxProjection> remove(
      SqlClient conn, long id) {
    return outboxRepository.delete(conn, id);
  }
}
