/* Licensed under Apache-2.0 2024. */
package com.example.commons.jooq;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.conf.StatementType;
import org.jooq.impl.DSL;

@Module
final class PreparedStatementDslContextConfig {

  private PreparedStatementDslContextConfig() {}

  @Provides
  @Singleton
  @Named("prepared")
  static DSLContext dslContext() {
    Settings settings = new Settings().withStatementType(StatementType.PREPARED_STATEMENT);
    return DSL.using(SQLDialect.POSTGRES, settings);
  }
}
