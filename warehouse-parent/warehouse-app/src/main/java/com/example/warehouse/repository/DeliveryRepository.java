/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.repository;

import com.example.warehouse.repository.DeliveryProjectionFactory.FindNextDeliveryJobProjection.NextDeliveryJobProjection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlClient;
import java.util.List;
import java.util.UUID;

public interface DeliveryRepository {

  Future<List<NextDeliveryJobProjection>> findNextDeliveryJob(SqlClient conn, UUID truckId);
}
