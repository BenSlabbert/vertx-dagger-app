/* Licensed under Apache-2.0 2023. */
package com.example.commons.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.impl.NoStackTraceException;

public final class ProtobufParser {

  private ProtobufParser() {}

  public static <T extends GeneratedMessageV3> T parse(byte[] in, T instance) {
    try {
      return (T) instance.getParserForType().parseFrom(in);
    } catch (InvalidProtocolBufferException e) {
      throw new NoStackTraceException(e);
    }
  }
}
