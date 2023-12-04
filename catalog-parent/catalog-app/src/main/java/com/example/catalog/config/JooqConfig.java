/* Licensed under Apache-2.0 2023. */
package com.example.catalog.config;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import javax.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

@Module
class JooqConfig {

  private static final Logger log = LoggerFactory.getLogger(JooqConfig.class);

  private JooqConfig() {}

  @Provides
  @Singleton
  static DSLContext providesDslContext() {
    log.info("creating dsl context");
    Settings settings = new Settings().withStatementType(StatementType.STATIC_STATEMENT);
    return DSL.using(SQLDialect.POSTGRES, settings);
  }
}
