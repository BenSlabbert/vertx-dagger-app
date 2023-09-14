/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.projections;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import lombok.Data;

@Data
@RowMapped
@DataObject
public class UserDataObject {

  // todo: add test using generated jooq classes to tes the column names
  @Column(name = "id")
  private long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;
}
