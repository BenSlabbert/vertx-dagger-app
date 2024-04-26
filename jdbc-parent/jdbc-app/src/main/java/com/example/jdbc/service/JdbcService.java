/* Licensed under Apache-2.0 2024. */
package com.example.jdbc.service;

import static org.jooq.conf.ParamType.INLINED;

import com.example.commons.transaction.blocking.SimpleTransactionProvider;
import com.example.jdbc.generator.entity.generated.jooq.tables.Person;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;

@Singleton
public class JdbcService {

  private final SimpleTransactionProvider transactionProvider;
  private final DSLContext dslContext;

  @Inject
  JdbcService(DataSource dataSource, DSLContext dslContext) {
    this.transactionProvider = new SimpleTransactionProvider(dataSource);
    this.dslContext = dslContext;
  }

  public void run() {
    try {
      Connection conn = transactionProvider.acquire();
      transactionProvider.begin(null);

      String sql =
          dslContext
              .select(Person.PERSON.ID)
              .from(Person.PERSON)
              .where(Person.PERSON.ID.eq(1L))
              .getSQL(INLINED);

      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet resultSet = ps.executeQuery();

      while (resultSet.next()) {
        System.out.println(resultSet.getLong(1));
      }

      transactionProvider.release(conn);
      transactionProvider.commit(null);
    } catch (Exception e) {
      transactionProvider.rollback(null);
    }
  }
}
