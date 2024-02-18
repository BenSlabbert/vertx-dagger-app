/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

public final class DoubleParser implements Parser<Double> {

  private DoubleParser() {}

  public static DoubleParser create() {
    return new DoubleParser();
  }

  @Override
  public Double parse(String value) {
    return Double.parseDouble(value);
  }

  @Override
  public Double parse(String value, Double defaultValue) {
    return null == value ? defaultValue : parse(value);
  }
}
