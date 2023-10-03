/* Licensed under Apache-2.0 2023. */
package com.example.commons;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;

public class Protobuf {

  private Protobuf() {}

  public static <T extends GeneratedMessageV3> T parse(byte[] in, T instance) {
    try {
      return (T) instance.getParserForType().parseFrom(in);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException(e);
    }
  }
}
