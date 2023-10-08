/* Licensed under Apache-2.0 2023. */
package com.example.commons.kafka.producer;

import com.google.protobuf.GeneratedMessageV3;
import org.apache.kafka.common.serialization.Serializer;

public class ProtobufSerializer implements Serializer<GeneratedMessageV3> {

  @Override
  public byte[] serialize(String s, GeneratedMessageV3 msg) {
    if (msg == null) {
      return new byte[] {};
    }

    return msg.toByteArray();
  }
}
