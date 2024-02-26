/* Licensed under Apache-2.0 2024. */
package com.example.warehouse.repository;

import static com.example.warehouse.generator.entity.generated.jooq.tables.Delivery.DELIVERY;
import static com.example.warehouse.generator.entity.generated.jooq.tables.Truck.TRUCK;

import com.example.commons.sql.Projection;
import com.example.warehouse.generator.entity.generated.jooq.enums.DeliveryStatus;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.AttachableQueryPart;
import org.jooq.DSLContext;

@Singleton
public class DeliveryProjectionFactory {

  private final DSLContext dsl;

  @Inject
  DeliveryProjectionFactory(DSLContext dsl) {
    this.dsl = dsl;
  }

  public FindNextDeliveryJobProjection creatFindNextDeliveryJobProjection(UUID truckId) {
    return new FindNextDeliveryJobProjection(truckId);
  }

  public class FindNextDeliveryJobProjection
      implements Projection<List<FindNextDeliveryJobProjection.NextDeliveryJobProjection>> {

    private final UUID truckId;

    public FindNextDeliveryJobProjection(UUID truckId) {
      this.truckId = truckId;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.select(DELIVERY.ID, DELIVERY.STATUS)
          .from(DELIVERY)
          .join(TRUCK)
          .on(DELIVERY.TRUCK_ID.eq(TRUCK.ID))
          .where(
              TRUCK
                  .IDENTIFIER
                  .eq(truckId)
                  .and(DELIVERY.STATUS.in(DeliveryStatus.ASSIGNED, DeliveryStatus.IN_TRANSIT)))
          .orderBy(DELIVERY.ID)
          .limit(2);
    }

    @Override
    public List<NextDeliveryJobProjection> parse(RowSet<Row> rowSet) {
      return StreamSupport.stream(rowSet.spliterator(), false)
          .map(
              row ->
                  new NextDeliveryJobProjection(
                      row.getLong(0), DeliveryStatus.lookupLiteral(row.getString(1))))
          .toList();
    }

    public record NextDeliveryJobProjection(long deliveryId, DeliveryStatus deliveryStatus) {}
  }
}
