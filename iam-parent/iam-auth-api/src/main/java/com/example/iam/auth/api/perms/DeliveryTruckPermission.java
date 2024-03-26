/* Licensed under Apache-2.0 2024. */
package com.example.iam.auth.api.perms;

final class DeliveryTruckPermission implements Permission {

  DeliveryTruckPermission() {}

  @Override
  public String name() {
    return "delivery-truck";
  }
}
