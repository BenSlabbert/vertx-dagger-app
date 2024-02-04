/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.web.route.handler;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

import com.example.commons.web.ResponseWriter;
import com.example.warehouse.api.WarehouseApi;
import com.example.warehouse.api.dto.GetNextDeliveryJobRequestDto;
import com.example.warehouse.web.SchemaValidatorDelegator;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApiHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiHandler.class);

  private final WarehouseApi warehouseApi;
  private final SchemaValidatorDelegator schemaValidatorDelegator;

  @Inject
  ApiHandler(WarehouseApi warehouseApi, SchemaValidatorDelegator schemaValidatorDelegator) {
    this.warehouseApi = warehouseApi;
    this.schemaValidatorDelegator = schemaValidatorDelegator;
  }

  public void getNextDeliveryJob(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    Boolean valid = schemaValidatorDelegator.validate(GetNextDeliveryJobRequestDto.class, body);

    if (Boolean.FALSE.equals(valid)) {
      log.error("invalid login request params");
      ResponseWriter.writeBadRequest(ctx);
      return;
    }

    warehouseApi
        .login(new GetNextDeliveryJobRequestDto(body))
        .onFailure(
            err -> {
              log.error("failed to login user", err);
              ResponseWriter.writeInternalError(ctx);
            })
        .onSuccess(dto -> ResponseWriter.write(ctx, dto, OK));
  }
}
