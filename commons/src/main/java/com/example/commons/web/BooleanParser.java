/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

public final class BooleanParser implements Parser<Boolean> {

  private BooleanParser() {}

  public static BooleanParser create() {
    return new BooleanParser();
  }

  @Override
  public Boolean parse(String value) {
    return Boolean.parseBoolean(value);
  }

  @Override
  public Boolean parse(String value, Boolean defaultValue) {
    return null == value ? defaultValue : parse(value);
  }
}
