/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

import java.util.Map;

public sealed interface Role permits AdminRole, SystemCliRole, UiSubRole {

  String name();

  static Map<String, Role> roleForName() {
    return Map.of(
        new AdminRole().name(), new AdminRole(),
        new SystemCliRole().name(), new SystemCliRole(),
        new UiSubRole().name(), new UiSubRole());
  }
}
