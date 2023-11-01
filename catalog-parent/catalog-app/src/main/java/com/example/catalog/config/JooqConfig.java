/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import lombok.extern.java.Log;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

@Log
@Module
class JooqConfig {

  private JooqConfig() {}

  @Provides
  @Singleton
  static DSLContext providesDslContext() {
    log.info("creating dsl context");
    Settings settings = new Settings().withStatementType(StatementType.STATIC_STATEMENT);
    return DSL.using(SQLDialect.POSTGRES, settings);
  }
}
