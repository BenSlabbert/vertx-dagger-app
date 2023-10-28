/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.consumer;

import io.vertx.core.buffer.Buffer;
import io.vertx.kafka.client.producer.KafkaHeader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConsumerUtils {

  private ConsumerUtils() {}

  public static Map<String, Buffer> headersAsMap(List<KafkaHeader> headers) {
    return headers.stream().collect(Collectors.toMap(KafkaHeader::key, KafkaHeader::value));
  }
}
