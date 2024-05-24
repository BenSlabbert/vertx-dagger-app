/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.repository.sql.projection;

import static com.example.reactivetest.generator.entity.generated.jooq.Sequences.PERSON_ID_SEQ;
import static com.example.reactivetest.generator.entity.generated.jooq.tables.Person.PERSON;

import github.benslabbert.vertxdaggercommons.sql.Projection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.List;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.AttachableQueryPart;
import org.jooq.DSLContext;

@Singleton
public class PersonProjectionFactory {

  private final DSLContext dsl;

  @Inject
  PersonProjectionFactory(@Named("static") DSLContext dsl) {
    this.dsl = dsl;
  }

  public InsertReturningProjection createInsertReturningProjection(Long id, String name) {
    return new InsertReturningProjection(id, name);
  }

  public NextIdProjection createNextIdProjection() {
    return new NextIdProjection();
  }

  public FindPersonProjection createFindPersonProjection(long limit) {
    return new FindPersonProjection(limit);
  }

  public class InsertReturningProjection implements Projection<PersonProjection> {

    private final Long id;
    private final String name;

    private InsertReturningProjection(Long id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.insertInto(PERSON)
          .columns(PERSON.ID, PERSON.NAME)
          .values(id, name)
          .returning(PERSON.ID, PERSON.NAME);
    }

    @Override
    public PersonProjection parse(RowSet<Row> rowSet) {
      Row row = rowSet.iterator().next();
      return new PersonProjection(row.getLong(0), row.getString(1));
    }
  }

  public class NextIdProjection implements Projection<Long> {

    private NextIdProjection() {}

    @Override
    public AttachableQueryPart getSql() {
      return dsl.select(PERSON_ID_SEQ.nextval());
    }

    @Override
    public Long parse(RowSet<Row> rowSet) {
      return rowSet.iterator().next().getLong(0);
    }
  }

  public class FindPersonProjection implements Projection<List<PersonProjection>> {

    private final long limit;

    private FindPersonProjection(long limit) {
      this.limit = limit;
    }

    @Override
    public AttachableQueryPart getSql() {
      return dsl.select(PERSON.ID, PERSON.NAME).from(PERSON).limit(limit);
    }

    @Override
    public List<PersonProjection> parse(RowSet<Row> rowSet) {
      return StreamSupport.stream(rowSet.spliterator(), false)
          .map(row -> new PersonProjection(row.getLong(0), row.getString(1)))
          .toList();
    }
  }

  public record PersonProjection(long id, String name) {}
}
