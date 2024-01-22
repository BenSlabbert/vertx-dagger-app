/* Licensed under Apache-2.0 2023. */
package com.example.payment.config;

import com.example.starter.jdbc.pool.JdbcPoolModule;
import dagger.Module;

@Module(includes = {JooqConfig.class, JdbcPoolModule.class})
public interface ConfigModule {}
