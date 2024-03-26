/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

import java.util.Map;

public sealed interface Permission
    permits AdminPermission, DeliveryTruckPermission, ReadWritePermission {

  String name();

  static Map<String, Permission> permissionForName() {
    return Map.of(
        new AdminPermission().name(), new AdminPermission(),
        new DeliveryTruckPermission().name(), new DeliveryTruckPermission(),
        new ReadWritePermission().name(), new ReadWritePermission());
  }
}
