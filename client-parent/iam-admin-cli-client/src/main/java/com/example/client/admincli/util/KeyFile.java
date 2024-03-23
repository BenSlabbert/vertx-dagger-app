/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.util;

import com.google.auto.value.AutoBuilder;

public record KeyFile(String username, char[] password, String token, String refreshToken) {

  public static Builder builder() {
    return new AutoBuilder_KeyFile_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder username(String username);

    Builder password(char[] password);

    Builder token(String token);

    Builder refreshToken(String refreshToken);

    KeyFile build();
  }
}
