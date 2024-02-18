/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

public final class FloatParser implements Parser<Float> {

  private FloatParser() {}

  public static FloatParser create() {
    return new FloatParser();
  }

  @Override
  public Float parse(String value) {
    return Float.parseFloat(value);
  }

  @Override
  public Float parse(String value, Float defaultValue) {
    return null == value ? defaultValue : parse(value);
  }
}
