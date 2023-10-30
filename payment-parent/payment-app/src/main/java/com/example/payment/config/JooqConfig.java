/* Licensed under Apache-2.0 2023. */
package com.example.payment.config;

import dagger.Module;
import dagger.Provides;
import javax.sql.DataSource;
import lombok.extern.java.Log;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

@Log
@Module
class JooqConfig {

  private JooqConfig() {}

  private static DSLContext dslContext = null;

  @Provides
  static synchronized DSLContext providesDslContext(DataSource dataSource) {
    if (dslContext != null) return dslContext;

    log.info("creating dsl context");
    Settings settings = new Settings().withFetchSize(128);
    dslContext = DSL.using(dataSource, SQLDialect.POSTGRES, settings);

    return dslContext;
  }
}
