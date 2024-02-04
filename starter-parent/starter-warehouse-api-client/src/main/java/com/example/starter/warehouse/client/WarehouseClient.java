/* Licensed under Apache-2.0 2024. */
package com.example.starter.warehouse.client;

import com.example.warehouse.api.WarehouseApi;
import com.example.warehouse.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.api.dto.GetNextDeliveryJobResponseDto;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class WarehouseClient implements WarehouseApi {

  private final WebClient webClient;

  @AssistedInject
  WarehouseClient(Vertx vertx, @Assisted String baseUrl, @Assisted int port) {
    this.webClient =
        WebClient.create(
            vertx, new WebClientOptions().setDefaultHost(baseUrl).setDefaultPort(port));
  }

  @Override
  public Future<GetNextDeliveryJobResponseDto> login(GetNextDeliveryJobRequestDto req) {
    return webClient
        .get("/next")
        .send()
        .map(b -> new GetNextDeliveryJobResponseDto(b.bodyAsJsonObject()));
  }
}
