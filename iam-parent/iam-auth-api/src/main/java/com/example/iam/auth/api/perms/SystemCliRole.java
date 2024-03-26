/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

final class SystemCliRole implements Role {

  SystemCliRole() {}

  @Override
  public String name() {
    return "system-cli";
  }
}
