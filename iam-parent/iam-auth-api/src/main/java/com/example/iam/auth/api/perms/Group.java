/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

import java.util.Map;

public sealed interface Group permits AdminGroup, SystemGroup, UserGroup {

  String name();

  static Map<String, Group> groupForName() {
    return Map.of(
        new AdminGroup().name(), new AdminGroup(),
        new SystemGroup().name(), new SystemGroup(),
        new UserGroup().name(), new UserGroup());
  }
}
