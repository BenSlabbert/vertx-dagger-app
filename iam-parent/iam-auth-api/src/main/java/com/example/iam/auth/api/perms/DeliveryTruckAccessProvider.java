/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

public final class DeliveryTruckAccessProvider {

  private DeliveryTruckAccessProvider() {}

  public static Access createAccess() {
    return Access.builder()
        .group(new SystemGroup())
        .role(new SystemCliRole())
        .addPermission(new DeliveryTruckPermission())
        .build();
  }
}
