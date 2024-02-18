/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

import java.time.Instant;

public final class InstantParser implements Parser<Instant> {

  private InstantParser() {}

  public static InstantParser create() {
    return new InstantParser();
  }

  @Override
  public Instant parse(String value) {
    Integer parse = IntegerParser.create().parse(value);

    if (null == parse) {
      return null;
    }

    return Instant.ofEpochMilli(parse);
  }

  @Override
  public Instant parse(String value, Instant defaultValue) {
    return null == value ? defaultValue : parse(value);
  }
}
