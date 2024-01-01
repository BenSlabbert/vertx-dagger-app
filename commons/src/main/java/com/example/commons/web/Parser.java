/* Licensed under Apache-2.0 2024. */
package com.example.commons.web;

public interface Parser<T> {

  T parse(String value);

  T parse(String value, T defaultValue);
}
