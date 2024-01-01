/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

import org.apache.commons.lang3.math.NumberUtils;

public class LongParser implements Parser<Long> {

  private LongParser() {}

  public static LongParser create() {
    return new LongParser();
  }

  @Override
  public Long parse(String value) {
    return NumberUtils.isCreatable(value) ? NumberUtils.createLong(value) : null;
  }

  @Override
  public Long parse(String value, Long defaultValue) {
    return NumberUtils.isCreatable(value) ? NumberUtils.createLong(value) : defaultValue;
  }
}
