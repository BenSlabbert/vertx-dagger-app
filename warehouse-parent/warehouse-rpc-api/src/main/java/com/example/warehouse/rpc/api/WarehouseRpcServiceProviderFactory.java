/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.rpc.api;

import dagger.assisted.AssistedFactory;
import io.vertx.core.eventbus.DeliveryOptions;

@AssistedFactory
public interface WarehouseRpcServiceProviderFactory {

  WarehouseRpcServiceProvider create(DeliveryOptions deliveryOptions);
}
