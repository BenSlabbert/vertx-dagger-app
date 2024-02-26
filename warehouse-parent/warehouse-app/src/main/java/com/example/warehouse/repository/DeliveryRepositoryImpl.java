/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.repository;

import static com.example.commons.sql.ProjectionExecutor.execute;

import com.example.warehouse.repository.DeliveryProjectionFactory.FindNextDeliveryJobProjection.NextDeliveryJobProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class DeliveryRepositoryImpl implements DeliveryRepository {

  private final DeliveryProjectionFactory deliveryProjectionFactory;

  @Inject
  DeliveryRepositoryImpl(DeliveryProjectionFactory deliveryProjectionFactory) {
    this.deliveryProjectionFactory = deliveryProjectionFactory;
  }

  @Override
  public Future<List<NextDeliveryJobProjection>> findNextDeliveryJob(SqlClient conn, UUID truckId) {
    return execute(conn, deliveryProjectionFactory.creatFindNextDeliveryJobProjection(truckId));
  }
}
