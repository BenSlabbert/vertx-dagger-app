/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

final class AdminGroup implements Group {

  AdminGroup() {}

  @Override
  public String name() {
    return "admin";
  }
}
