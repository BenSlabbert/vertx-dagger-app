/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

final class UiSubRole implements Role {

  UiSubRole() {}

  @Override
  public String name() {
    return "ui";
  }
}
