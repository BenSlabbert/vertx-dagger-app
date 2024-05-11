/* Licensed under Apache-2.0 2024. */
package com.example.commons.jooq;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Module
final class DataSourceDslContextConfig {

  private static final Logger log = LoggerFactory.getLogger(DataSourceDslContextConfig.class);

  private DataSourceDslContextConfig() {}

  @Provides
  @Singleton
  static DSLContext dslContext(DataSource dataSource) {
    Settings settings = new Settings().withFetchSize(128);
    DSLContext dslContext = DSL.using(dataSource, SQLDialect.POSTGRES, settings);

    int execute = dslContext.execute("SELECT 1");
    log.info("init dsl context: executed select 1: " + execute);

    return dslContext;
  }
}
