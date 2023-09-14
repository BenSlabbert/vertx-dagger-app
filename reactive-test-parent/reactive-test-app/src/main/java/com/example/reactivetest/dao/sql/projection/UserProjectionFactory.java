/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.dao.sql.projection;

import static com.example.reactivetest.generator.entity.generated.jooq.tables.UserData.USER_DATA;
import static org.jooq.conf.ParamType.INLINED;

import com.example.reactivetest.projections.UserDataObject;
import com.example.reactivetest.projections.UserDataObjectRowMapper;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.jooq.DSLContext;

@Singleton
public class UserProjectionFactory {

  private final DSLContext dsl;

  @Inject
  public UserProjectionFactory(DSLContext dsl) {
    this.dsl = dsl;
  }

  public UserProjection createUserProjection() {
    return new UserProjection();
  }

  public class UserProjection implements Projection<List<UserDataObject>> {

    @Override
    public String getSql() {
      return dsl.select(USER_DATA.ID, USER_DATA.FIRST_NAME, USER_DATA.LAST_NAME)
          .from(USER_DATA)
          .orderBy(USER_DATA.ID.asc())
          .getSQL(INLINED);
    }

    @Override
    public List<UserDataObject> parse(RowSet<Row> rowSet) {
      return StreamSupport.stream(rowSet.spliterator(), false)
          .collect(UserDataObjectRowMapper.COLLECTOR);
    }
  }
}
