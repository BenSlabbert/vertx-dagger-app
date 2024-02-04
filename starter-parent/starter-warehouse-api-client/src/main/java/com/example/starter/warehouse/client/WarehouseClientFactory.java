/* Licensed under Apache-2.0 2024. */
package com.example.starter.warehouse.client;

import dagger.assisted.AssistedFactory;

@AssistedFactory
public interface WarehouseClientFactory {

  WarehouseClient create(String baseUrl, int port);
}
