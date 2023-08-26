package com.example.reactivetest.proto;

public enum Version {
  V1("v1");

  private final String value;

  Version(String value) {
    this.value = value;
  }

  public static Version parse(String in) {
    for (Version v : Version.values()) {
      if (v.value.equals(in)) {
        return v;
      }
    }

    throw new IllegalArgumentException("unable to get version from: " + in);
  }

  @Override
  public String toString() {
    return value;
  }
}
