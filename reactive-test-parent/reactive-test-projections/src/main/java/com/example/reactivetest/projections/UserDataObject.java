/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.projections;

import com.google.auto.value.AutoBuilder;
import github.benslabbert.vertxdaggercodegen.annotation.projection.Column;
import github.benslabbert.vertxdaggercodegen.annotation.projection.ReactiveProjection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.stream.StreamSupport;

@ReactiveProjection
public record UserDataObject(
    @Column(name = "id") long id,
    @Column(name = "first_name") String firstName,
    @Column(name = "last_name") String lastName) {

  public static UserDataObject map(Row row) {
    return UserDataObject_ReactiveRowMapper.INSTANCE.map(row);
  }

  public static List<UserDataObject> map(RowSet<Row> rowSet) {
    return StreamSupport.stream(rowSet.spliterator(), false)
        .collect(UserDataObject_ReactiveRowMapper.COLLECTOR);
  }

  public static Builder builder() {
    return new AutoBuilder_UserDataObject_Builder();
  }

  @AutoBuilder
  public interface Builder {
    Builder id(long id);

    Builder firstName(String firstName);

    Builder lastName(String lastName);

    UserDataObject build();
  }
}
