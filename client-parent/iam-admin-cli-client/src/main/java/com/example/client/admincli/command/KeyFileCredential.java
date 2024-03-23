/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import java.io.File;
import picocli.CommandLine.Option;

public class KeyFileCredential {

  KeyFileCredential() {}

  @Option(
      required = true,
      names = {"-kf", "--key-file"},
      description = "user's key file with existing credentials")
  private File file;

  public File file() {
    return file;
  }
}
