/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

public final class UserAccessProvider {

  private UserAccessProvider() {}

  public static Access createAccess() {
    return Access.builder()
        .group(new UserGroup())
        .role(new UiSubRole())
        .addPermission(new ReadWritePermission())
        .build();
  }
}
