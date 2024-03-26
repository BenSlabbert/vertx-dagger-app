/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

final class SystemGroup implements Group {

  SystemGroup() {}

  @Override
  public String name() {
    return "system";
  }
}
