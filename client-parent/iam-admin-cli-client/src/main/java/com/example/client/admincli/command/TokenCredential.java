/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import picocli.CommandLine.Option;

public class TokenCredential {

  TokenCredential() {}

  @Option(
      required = true,
      names = {"-u", "--username"},
      description = "user's username")
  private String username;

  @Option(
      required = true,
      names = {"-rt", "--refresh-token"},
      description = "users refresh token")
  private String token;

  public String username() {
    return username;
  }

  public String token() {
    return token;
  }
}
