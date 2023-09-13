package com.example.reactivetest.projections;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.sqlclient.templates.annotations.Column;
import io.vertx.sqlclient.templates.annotations.RowMapped;
import lombok.Data;

@Data
@RowMapped
@DataObject
public class UserDataObject {

  @Column(name = "id")
  private long id;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;
}
