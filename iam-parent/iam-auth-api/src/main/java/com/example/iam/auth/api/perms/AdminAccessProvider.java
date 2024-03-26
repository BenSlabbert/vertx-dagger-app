/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

public final class AdminAccessProvider {

  private AdminAccessProvider() {}

  public static Access createAccess() {
    return Access.builder()
        .group(new AdminGroup())
        .role(new AdminRole())
        .addPermission(new AdminPermission())
        .build();
  }
}
