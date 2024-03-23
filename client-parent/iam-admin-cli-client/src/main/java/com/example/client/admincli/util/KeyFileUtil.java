/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.util;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.nio.file.Path;

public final class KeyFileUtil {

  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String TOKEN = "token";
  private static final String REFRESH_TOKEN = "refreshToken";

  private KeyFileUtil() {}

  public static void write(Vertx vertx, Path path, KeyFile keyFile) {
    JsonObject object = new JsonObject();
    object.put(USERNAME, keyFile.username());
    object.put(PASSWORD, String.valueOf(keyFile.password()));
    object.put(TOKEN, keyFile.token());
    object.put(REFRESH_TOKEN, keyFile.refreshToken());

    vertx.fileSystem().writeFileBlocking(path.toString(), object.toBuffer());
  }

  public static KeyFile parse(Vertx vertx, File keyFile) {
    Buffer buffer = vertx.fileSystem().readFileBlocking(keyFile.getAbsolutePath());
    JsonObject object = new JsonObject(buffer);

    var username = object.getString(USERNAME);
    var password = object.getString(PASSWORD, "");
    var token = object.getString(TOKEN);
    var refreshToken = object.getString(REFRESH_TOKEN);

    return KeyFile.builder()
        .username(username)
        .password(password.toCharArray())
        .token(token)
        .refreshToken(refreshToken)
        .build();
  }

  public static void updateTokens(Vertx vertx, File keyFile, String token, String refreshToken) {
    Buffer buffer = vertx.fileSystem().readFileBlocking(keyFile.getAbsolutePath());
    JsonObject object = new JsonObject(buffer);

    object.put(TOKEN, token);
    object.put(REFRESH_TOKEN, refreshToken);

    vertx.fileSystem().writeFileBlocking(keyFile.getAbsolutePath(), object.toBuffer());
  }
}
