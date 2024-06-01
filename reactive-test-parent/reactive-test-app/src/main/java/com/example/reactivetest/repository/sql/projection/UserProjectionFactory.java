/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.repository.sql.projection;

import static com.example.reactivetest.generator.entity.generated.jooq.tables.UserData.USER_DATA;

import com.example.reactivetest.projections.UserDataObject;
import github.benslabbert.vertxdaggercommons.sql.Projection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.AttachableQueryPart;
import org.jooq.DSLContext;

@Singleton
public class UserProjectionFactory {

  private final DSLContext dsl;

  @Inject
  public UserProjectionFactory(@Named("static") DSLContext dsl) {
    this.dsl = dsl;
  }

  public UserProjection createUserProjection() {
    return new UserProjection();
  }

  public class UserProjection implements Projection<List<UserDataObject>> {

    @Override
    public AttachableQueryPart getSql() {
      return dsl.select(USER_DATA.ID, USER_DATA.FIRST_NAME, USER_DATA.LAST_NAME)
          .from(USER_DATA)
          .orderBy(USER_DATA.ID.asc());
    }

    @Override
    public List<UserDataObject> parse(RowSet<Row> rowSet) {
      return UserDataObject.map(rowSet);
    }
  }
}
