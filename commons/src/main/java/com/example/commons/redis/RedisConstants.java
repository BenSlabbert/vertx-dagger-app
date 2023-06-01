package com.example.commons.redis;

public class RedisConstants {

  public static final String OK = "OK";
  public static final String STREAM_GENERATE_ID = "*";
  public static final String QUEUED = "QUEUED";
  public static final String DOCUMENT_ROOT = "$";
  public static final String SET_IF_DOES_NOT_EXIST = "NX";
  public static final String SET_IF_EXIST = "XX";
  public static final String CONSUME_NEW_MESSAGES = ">";

  private RedisConstants() {}
}
