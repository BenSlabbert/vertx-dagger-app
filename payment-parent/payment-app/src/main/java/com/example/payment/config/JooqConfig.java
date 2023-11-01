/* Licensed under Apache-2.0 2023. */
package com.example.payment.config;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
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

  @Provides
  @Singleton
  static DSLContext dslContext(DataSource dataSource) {
    log.info("creating dsl context");
    Settings settings = new Settings().withFetchSize(128);
    DSLContext dslContext = DSL.using(dataSource, SQLDialect.POSTGRES, settings);

    int execute = dslContext.execute("SELECT 1");
    log.info("init dsl context: executed select 1: " + execute);

    return dslContext;
  }
}
