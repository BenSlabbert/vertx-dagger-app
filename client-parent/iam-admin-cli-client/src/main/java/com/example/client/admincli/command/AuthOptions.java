/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import picocli.CommandLine.ArgGroup;

class AuthOptions {

  AuthOptions() {}

  @ArgGroup(exclusive = false, multiplicity = "1")
  KeyFileCredential keyFile;

  @ArgGroup(exclusive = false, multiplicity = "1")
  TokenCredential basicAuth;

  CredentialProvider toCredentials() {
    if (keyFile != null) {
      return CredentialProvider.keyFileCredential(keyFile);
    }
    return CredentialProvider.tokenCredential(basicAuth);
  }
}
