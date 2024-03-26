/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

final class ReadWritePermission implements Permission {

  ReadWritePermission() {}

  @Override
  public String name() {
    return "rw";
  }
}
