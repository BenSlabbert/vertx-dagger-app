/* Licensed under Apache-2.0 2023. */
package com.example.reactivetest.config;

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

  private static DSLContext dslContext = null;

  @Singleton
  @Provides
  static DSLContext providesDslContext() {
    if (dslContext != null) return dslContext;

    log.info("creating dsl context");
    Settings settings = new Settings().withStatementType(StatementType.STATIC_STATEMENT);
    dslContext = DSL.using(SQLDialect.POSTGRES, settings);
    return dslContext;
  }
}
