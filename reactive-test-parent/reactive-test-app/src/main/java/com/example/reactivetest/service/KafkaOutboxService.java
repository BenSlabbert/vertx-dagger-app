/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.service;

import com.example.reactivetest.proto.core.Header;
import com.example.reactivetest.proto.core.Headers;
import com.example.reactivetest.repository.sql.OutboxRepository;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory.DeleteFromOutbox.DeleteOutboxProjection;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory.GetFromOutboxProjection;
import com.example.reactivetest.repository.sql.projection.OutboxProjectionFactory.InsertIntoOutbox.InsertOutboxProjection;
import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.Future;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class KafkaOutboxService {

  private final OutboxRepository outboxRepository;

  @Inject
  KafkaOutboxService(OutboxRepository outboxRepository) {
    this.outboxRepository = outboxRepository;
  }

  Future<InsertOutboxProjection> insert(
      SqlClient conn, KafkaProducerRecord<String, ? extends GeneratedMessageV3> msg) {

    String key = msg.key();
    byte[] value = msg.value().toByteArray();
    List<Header> headerList =
        msg.headers().stream()
            .map(h -> Header.newBuilder().setKey(h.key()).setValue(h.value().toString()).build())
            .toList();

    byte[] headers = Headers.newBuilder().addAllHeaders(headerList).build().toByteArray();

    return outboxRepository.insert(conn, key, headers, value);
  }

  public Future<GetFromOutboxProjection> get(SqlClient conn, long id) {
    return outboxRepository.get(conn, id);
  }

  public Future<Optional<GetFromOutboxProjection>> next(SqlClient conn) {
    return outboxRepository.next(conn);
  }

  Future<DeleteOutboxProjection> remove(SqlClient conn, long id) {
    return outboxRepository.delete(conn, id);
  }
}
