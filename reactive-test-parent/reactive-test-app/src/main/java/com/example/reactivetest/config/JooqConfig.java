package com.example.reactivetest.config;

import dagger.Module;
import dagger.Provides;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

@Module
public class JooqConfig {

  private JooqConfig() {}

  @Provides
  static DSLContext providesDslContext() {
    Settings settings = new Settings().withStatementType(StatementType.STATIC_STATEMENT);
    return DSL.using(SQLDialect.POSTGRES, settings);
  }
}
