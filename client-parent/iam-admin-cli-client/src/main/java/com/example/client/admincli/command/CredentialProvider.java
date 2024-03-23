/* Licensed under Apache-2.0 2024. */
package com.example.client.admincli.command;

import com.google.auto.value.AutoOneOf;

@AutoOneOf(CredentialProvider.Kind.class)
abstract class CredentialProvider {

  enum Kind {
    KEY_FILE,
    TOKEN
  }

  abstract Kind getKind();

  public abstract KeyFileCredential keyFile();

  public abstract TokenCredential token();

  public static CredentialProvider keyFileCredential(KeyFileCredential credential) {
    return AutoOneOf_CredentialProvider.keyFile(credential);
  }

  public static CredentialProvider tokenCredential(TokenCredential credential) {
    return AutoOneOf_CredentialProvider.token(credential);
  }
}
